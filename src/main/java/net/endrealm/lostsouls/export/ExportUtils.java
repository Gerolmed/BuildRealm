package net.endrealm.lostsouls.export;

import net.endrealm.lostsouls.data.entity.Piece;
import net.endrealm.lostsouls.data.entity.Theme;
import net.endrealm.lostsouls.data.entity.TypeCategory;
import net.endrealm.lostsouls.repository.DataProvider;
import net.endrealm.lostsouls.services.ThreadService;
import net.endrealm.lostsouls.utils.Pair;
import net.endrealm.lostsouls.utils.Touple;
import net.endrealm.lostsouls.world.WorldService;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ExportUtils {

    public static Process<Theme> createExportChain(ThreadService threadService, DataProvider dataProvider, WorldService worldService, Runnable onFinish) {
        ProcessBuilder<Theme> mainBuilder = ProcessBuilder.builder(threadService, Theme.class);
        ProcessBuilder<Pair<Theme, File>> themeProcessBuilder = mainBuilder
                .initSubChains(
                        theme -> {
                            File file = new File(Bukkit.getWorldContainer(), "themes"+File.separator+theme.getName());
                            if(file.mkdirs()) {
                                System.out.println("made theme directory for " + theme.getName());
                            }

                            return Collections.singletonList(Pair.of(theme, file));
                        }
                ).nextAsync(theme -> {
                    //TODO do theme settings
                });

        ProcessBuilder<Touple<TypeCategory, File, Theme>> typeBuilder = createCategoryExportChain(themeProcessBuilder);

        ProcessBuilder<Pair<Piece, File>> pieceBuilder = createPieceExportChain(typeBuilder, dataProvider, threadService, worldService);
        pieceBuilder.build();
        typeBuilder.build();
        themeProcessBuilder.build();
        return mainBuilder.next(theme -> onFinish.run()).build();
    }

    private static ProcessBuilder<Pair<Piece, File>> createPieceExportChain(ProcessBuilder<Touple<TypeCategory, File, Theme>> typeBuilder, DataProvider provider, ThreadService threadService, WorldService worldService) {
        return typeBuilder
                .initSubChains(parent ->
                        provider
                        .getDraftsByThemeAndType(parent.getCompanion().getName(), parent.getKey().getType())
                                .stream()
                                .filter(draft -> draft instanceof Piece)
                                .map(draft -> (Piece) draft)
                                .map(piece -> {
                                    File file = new File(parent.getValue(), piece.getEffectiveName(provider)+".schem");
                                    return Pair.of(piece, file);
                                })
                .collect(Collectors.toList()))
                .nextRaw(new SchematicSaveProcess(threadService, worldService));
    }

    private static ProcessBuilder<Touple<TypeCategory, File, Theme>> createCategoryExportChain(ProcessBuilder<Pair<Theme, File>> themeProcessBuilder) {
        return themeProcessBuilder.initSubChains(theme -> theme.getKey().getCategoryList().stream().map(typeCategory -> {
            File file = new File(theme.getValue(), typeCategory.getType().toString().toLowerCase());
            if(file.mkdir()) {
                System.out.println("made file directory for type " + typeCategory.getType());
            }
            return Touple.of(typeCategory, file, theme.getKey());
        }).collect(Collectors.toList()))
                .nextAsync(categoryPair -> {
                    //TODO add category settings?
                });
    }
}
