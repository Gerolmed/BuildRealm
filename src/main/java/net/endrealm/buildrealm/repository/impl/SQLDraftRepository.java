package net.endrealm.buildrealm.repository.impl;

import lombok.Data;
import net.endrealm.buildrealm.data.PermissionLevel;
import net.endrealm.buildrealm.data.PieceType;
import net.endrealm.buildrealm.data.entity.Draft;
import net.endrealm.buildrealm.data.entity.ForkData;
import net.endrealm.buildrealm.data.entity.Member;
import net.endrealm.buildrealm.data.entity.Piece;
import net.endrealm.buildrealm.repository.DraftRepository;
import net.endrealm.realmdrive.interfaces.DriveService;
import net.endrealm.realmdrive.query.Query;
import net.endrealm.realmdrive.query.compare.ValueNotInOperator;
import net.endrealm.realmdrive.query.logics.AndOperator;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Data
public abstract class SQLDraftRepository implements DraftRepository {

    private final Logger logger;
    protected Connection connection;
    // The name of the table we created back in SQLite class.
    private final String table = "drafts";

    public SQLDraftRepository(Logger logger) {

        this.logger = logger;
    }

    @Override
    public synchronized Optional<Draft> findByKey(String id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE id = ?;");
            ps.setString(1, id);
            rs = ps.executeQuery();
            if(rs.next()){
                return Optional.of(readFromResult(rs));
            }
        } catch (SQLException ex) {
            getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return Optional.empty();
    }

    private Draft readFromResult(ResultSet resultSet) throws SQLException {
        Draft draft;

        if(resultSet.getString("pieceType") != null) {
            var piece = new Piece();
            piece.setPieceType(PieceType.valueOf(resultSet.getString("pieceType")));
            piece.setNumber(resultSet.getString("number"));
            piece.setForkCount(resultSet.getInt("forkCount"));
            draft = piece;
        } else {
            draft = new Draft();
        }

        draft.setId(resultSet.getString("id"));
        draft.setGroup(resultSet.getString("group"));
        if(resultSet.getString("forkId") != null) {
            draft.setForkData(new ForkData(resultSet.getString("forkId")));
        }
        draft.setMembers(Arrays.stream(resultSet.getString("membersRaw").split(":"))
                        .map(row -> {
                            if(row.length() == 0) return null;
                            var parts = row.split(",");
                            return new Member(UUID.fromString(parts[0]), PermissionLevel.valueOf(parts[1]));
                        })
                        .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new)));
        draft.setNote(resultSet.getString("note"));
        draft.setLastUpdated(resultSet.getTimestamp("lastUpdated"));
        draft.setOpen(resultSet.getInt("open") == 1);

        return draft;
    }


    // { $and: [{ "members.uuid": { $eq: "1234" }}, { id: { $nin: ["hijklm", "nopqrs"] } }] }
    @Override
    public synchronized List<Draft> findByMember(UUID uuid, boolean open, List<String> excludedDraftIds) {
        var results = new ArrayList<Draft>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE members LIKE ? AND `open` = 1 AND id NOT IN ("+ excludedDraftIds.stream().map(s -> "?").collect(Collectors.joining(",")) +");");
            ps.setString(1, "%:"+ uuid +":%");

            for (int i = 0; i < excludedDraftIds.size(); i++) {
                ps.setString(2+i, excludedDraftIds.get(i));
            }
            rs = ps.executeQuery();
            while(rs.next()){
                results.add(readFromResult(rs));
            }
        } catch (SQLException ex) {
            getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return results;
    }

    @Override
    public synchronized void save(Draft value) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("INSERT OR REPLACE INTO " + table + " " +
                    "(id, members, membersRaw, `open`, `group`, lastUpdated, forkId, note, `number`, forkCount, pieceType)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
            ps.setString(1, value.getId());
            ps.setString(2, ":"+value.getMembers().stream().map(member -> member.getUuid().toString()).collect(Collectors.joining(":"))+ ":");
            ps.setString(3, ":"+value.getMembers().stream().map(member -> member.getUuid().toString() + "," + member.getPermissionLevel().toString()).collect(Collectors.joining(":"))+ ":");
            ps.setInt(4, value.isOpen() ? 1 : 0);
            ps.setString(5, value.getGroup());
            ps.setTimestamp(6, new Timestamp(value.getLastUpdated().getTime()));
            if(value.getForkData() == null) {
                ps.setNull(7, java.sql.Types.NULL);
            } else {
                ps.setString(7, value.getForkData().getOriginId());
            }
            ps.setString(8, value.getNote());

            if(value instanceof Piece) {
                var piece = (Piece) value;
                ps.setString(9, piece.getNumber());
                ps.setInt(10, piece.getForkCount());
                ps.setString(11, piece.getPieceType().toString());
            } else {

                ps.setNull(9, java.sql.Types.NULL);
                ps.setNull(10, java.sql.Types.NULL);
                ps.setNull(11, java.sql.Types.NULL);
            }
            ps.execute();
        } catch (SQLException ex) {
            getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    @Override
    public String findFreeKey() {
        String id;
        outer:
        do {
            String fullId = UUID.randomUUID().toString().replace("-", "");

            int i = 4;
            do {
                id = fullId.substring(0, i);
                i++;
                if (!containsId(id))
                    break outer;
            } while (fullId.length() > id.length());
        } while (true);

        return id;
    }

    private boolean containsId(String id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT id FROM " + table + " WHERE id = ?;");
            ps.setString(1, id);
            rs = ps.executeQuery();
            if(rs.next()){
                return true;
            }
        } catch (SQLException ex) {
            getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return false;
    }

    @Override
    public synchronized void remove(Draft draft) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("DELETE FROM " + table + " WHERE id = ?;");
            ps.setString(1, draft.getId());
            ps.execute();
        } catch (SQLException ex) {
            getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    @Override
    public List<Draft> findByGroupAndType(String group, PieceType type, List<String> excludedDraftIds) {

        var results = new ArrayList<Draft>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE pieceType = ? AND `group` = ? AND id NOT IN ("+ excludedDraftIds.stream().map(s -> "?").collect(Collectors.joining(",")) +");");
            ps.setString(1, type.toString());
            ps.setString(2, group);
            for (int i = 0; i < excludedDraftIds.size(); i++) {
                ps.setString(3+i, excludedDraftIds.get(i));
            }
            rs = ps.executeQuery();
            while(rs.next()){
                results.add(readFromResult(rs));
            }
        } catch (SQLException ex) {
            getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return results;

    }

    @Override
    public List<Draft> findByParent(String parentId, List<String> excludedDraftIds) {

        var results = new ArrayList<Draft>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE forkId = ? AND id NOT IN ("+ excludedDraftIds.stream().map(s -> "?").collect(Collectors.joining(",")) +");");
            ps.setString(1, parentId);
            for (int i = 0; i < excludedDraftIds.size(); i++) {
                ps.setString(2+i, excludedDraftIds.get(i));
            }
            rs = ps.executeQuery();
            while(rs.next()){
                results.add(readFromResult(rs));
            }
        } catch (SQLException ex) {
            getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return results;
    }


    public abstract Connection getSQLConnection();

    public abstract void load();

    public void initialize(){

    }

    public void close(PreparedStatement ps, ResultSet rs){
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (SQLException ex) {
            getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}
