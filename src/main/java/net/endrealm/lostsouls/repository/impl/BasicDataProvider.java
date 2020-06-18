package net.endrealm.lostsouls.repository.impl;

import lombok.Data;
import net.endrealm.lostsouls.data.PieceType;
import net.endrealm.lostsouls.data.entity.Draft;
import net.endrealm.lostsouls.data.entity.Piece;
import net.endrealm.lostsouls.data.entity.Theme;
import net.endrealm.lostsouls.repository.Cache;
import net.endrealm.lostsouls.repository.DataProvider;
import net.endrealm.lostsouls.repository.DraftRepository;
import net.endrealm.lostsouls.repository.ThemeRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("SimplifyOptionalCallChains")
@Data
public class BasicDataProvider implements DataProvider {

    private final Cache<Draft, String> draftCache;
    private final DraftRepository draftRepository;
    private final Cache<Theme, String> themeCache;
    private final ThemeRepository themeRepository;
    @Override
    public Optional<Draft> getDraft(String key) {
        Optional<Draft> draftOpt = draftCache.get(key);
        if(!draftOpt.isPresent()) {
            draftOpt = draftRepository.findByKey(key);
            draftOpt.ifPresent(draft -> draftCache.add(draft.getId(), draft));
        }
        return draftOpt;
    }

    @Override
    public List<Draft> getDraftsByUser(UUID uuid) {
        List<Draft> drafts = draftCache.getAllBy(value -> value.hasMember(uuid));

        List<Draft> newDrafts = draftRepository.findByMember(uuid, true, drafts.stream().map(Draft::getId).collect(Collectors.toList()));
        newDrafts.forEach(draft -> draftCache.add(draft.getId(), draft));
        drafts.addAll(newDrafts);
        return drafts;
    }

    @Override
    public synchronized void saveDraft(Draft draft) {
        draftCache.add(draft.getId(), draft);
        draftRepository.save(draft);
    }

    @Override
    public synchronized String getFreeDraftId() {
        return draftRepository.findFreeKey();
    }

    @Override
    public void remove(Draft draft) {
        draftCache.markDirty(draft.getId());
        draftRepository.remove(draft);
    }

    @Override
    public Optional<Theme> getTheme(String key) {
        Optional<Theme> draftOpt = themeCache.get(key);
        if(!draftOpt.isPresent()) {
            draftOpt = themeRepository.get(key);
            draftOpt.ifPresent(draft -> themeCache.add(draft.getName(), draft));
        }
        return draftOpt;
    }

    @Override
    public void saveTheme(Theme theme) {
        themeCache.add(theme.getName(), theme);
        themeRepository.save(theme);
    }

    @Override
    public List<Theme> getAllThemes() {
        List<Theme> cachedThemes = themeCache.getAllBy(value -> true);
        List<Theme> newThemes = themeRepository.getAll(cachedThemes.stream().map(Theme::getName).collect(Collectors.toList()));
        newThemes.forEach(theme -> themeCache.add(theme.getName(), theme));
        cachedThemes.addAll(newThemes);
        return cachedThemes;
    }

    @Override
    public void removeTheme(Theme theme) {
        themeCache.markDirty(theme.getName());
        themeRepository.delete(theme.getName());
    }

    @Override
    public List<Draft> getDraftsByThemeAndType(String theme, PieceType type) {
        List<Draft> drafts = draftCache.getAllBy(value -> {
            if(!(value instanceof Piece)) return false;
            Piece piece = (Piece) value;

            return piece.getTheme().equals(theme) && piece.getPieceType() == type;
        });

        List<Draft> newDrafts = draftRepository.findByThemeAndType(theme, type, drafts.stream().map(Draft::getId).collect(Collectors.toList()));
        newDrafts.forEach(draft -> draftCache.add(draft.getId(), draft));
        drafts.addAll(newDrafts);
        return drafts;
    }
}
