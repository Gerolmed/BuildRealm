package net.endrealm.buildrealm.export;

import com.google.common.io.Files;
import net.endrealm.buildrealm.data.entity.Group;
import net.endrealm.buildrealm.data.entity.Piece;
import net.endrealm.buildrealm.data.entity.TypeCategory;
import net.endrealm.buildrealm.repository.DataProvider;
import net.endrealm.buildrealm.services.ThreadService;
import net.endrealm.buildrealm.utils.Pair;
import net.endrealm.buildrealm.utils.Touple;
import net.endrealm.buildrealm.world.WorldService;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;

public final class ExportUtils {

    @SuppressWarnings("UnstableApiUsage")
    public static Process<Group> createExportChain(File dataFolder, ThreadService threadService, DataProvider dataProvider, WorldService worldService, Runnable onFinish) {
        ProcessBuilder<Group> mainBuilder = ProcessBuilder.builder(threadService, Group.class);
        ProcessBuilder<Pair<Group, File>> GroupProcessBuilder = mainBuilder
                .initSubChains(
                        group -> {
                            File file = new File(Bukkit.getWorldContainer(), "Groups" + File.separator + group.getName());
                            if (file.mkdirs()) {
                                System.out.println("made group directory for " + group.getName());
                            }

                            return Collections.singletonList(Pair.of(group, file));
                        }
                ).nextAsync(group -> {
                    File settings = new File(group.getValue(), "settings.yml");
                    File defaultFiles = new File(dataFolder, "GroupDefaultSettings.yml");

                    if (!settings.exists()) {
                        try {
                            Files.copy(defaultFiles, settings);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

        ProcessBuilder<Touple<TypeCategory, File, Group>> typeBuilder = createCategoryExportChain(GroupProcessBuilder);

        ProcessBuilder<Pair<Piece, File>> pieceBuilder = createPieceExportChain(typeBuilder, dataProvider, threadService, worldService);
        pieceBuilder.build();
        typeBuilder.build();
        GroupProcessBuilder.build();
        return mainBuilder.next(group -> onFinish.run()).build();
    }

    private static ProcessBuilder<Pair<Piece, File>> createPieceExportChain(ProcessBuilder<Touple<TypeCategory, File, Group>> typeBuilder, DataProvider provider, ThreadService threadService, WorldService worldService) {
        return typeBuilder
                .initSubChains(parent ->
                        provider
                                .getDraftsByGroupAndType(parent.getCompanion().getName(), parent.getKey().getType())
                                .stream()
                                .filter(draft -> draft instanceof Piece)
                                .map(draft -> (Piece) draft)
                                .map(piece -> {
                                    File file = new File(parent.getValue(), piece.getEffectiveName(provider) + ".schem");
                                    return Pair.of(piece, file);
                                })
                                .collect(Collectors.toList()))
                .nextRaw(new SchematicSaveProcess(threadService, worldService));
    }

    private static ProcessBuilder<Touple<TypeCategory, File, Group>> createCategoryExportChain(ProcessBuilder<Pair<Group, File>> GroupProcessBuilder) {
        return GroupProcessBuilder.initSubChains(Group -> Group.getKey().getCategoryList().stream().map(typeCategory -> {
            File file = new File(Group.getValue(), typeCategory.getType().toString().toLowerCase());
            if (file.mkdir()) {
                System.out.println("made file directory for type " + typeCategory.getType());
            }
            return Touple.of(typeCategory, file, Group.getKey());
        }).collect(Collectors.toList()))
                .nextAsync(categoryPair -> {
                    //TODO add category settings?
                });
    }
}
