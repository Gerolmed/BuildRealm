package net.endrealm.buildrealm.repository.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import net.endrealm.buildrealm.data.PermissionLevel;
import net.endrealm.buildrealm.data.PieceType;
import net.endrealm.buildrealm.data.entity.*;
import net.endrealm.buildrealm.repository.DraftRepository;
import net.endrealm.buildrealm.repository.GroupRepository;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Data
public abstract class SQLGroupRepository implements GroupRepository {

    private final Logger logger;
    protected Connection connection;
    // The name of the table we created back in SQLite class.
    private final String table = "groups";
    private final Gson gson = new Gson();

    public SQLGroupRepository(Logger logger) {

        this.logger = logger;
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

    @Override
    public void delete(String id) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("DELETE FROM " + table + " WHERE id = ?;");
            ps.setString(1, id);
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
    public void save(Group group) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("INSERT OR REPLACE INTO " + table + " " +
                    "(id, stale, settings, categoryList)" +
                    "VALUES (?, ?, ?, ?);");
            ps.setString(1, group.getName());
            ps.setInt(2, group.isStale() ? 1 : 0);
            ps.setString(3, gson.toJson(group.getSettings()));
            ps.setString(4,  "["+group.getCategoryList().stream().map(gson::toJson).collect(Collectors.joining(",")) + "]");
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
    public Optional<Group> get(String id) {
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

    @Override
    public List<Group> getAll(List<String> except) {
        var results = new ArrayList<Group>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE id NOT IN ("+ except.stream().map(s -> "?").collect(Collectors.joining(",")) +");");


            for (int i = 0; i < except.size(); i++) {
                ps.setString(1+i, except.get(i));
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

    private Group readFromResult(ResultSet resultSet) throws SQLException {
        var group = new Group();

        group.setName(resultSet.getString("id"));
        group.setStale(resultSet.getInt("stale") == 1);
        group.setSettings(gson.fromJson(resultSet.getString("settings"), GroupSettings.class));
        var userListType = new TypeToken<ArrayList<TypeCategory>>(){}.getType();
        group.setCategoryList(gson.fromJson(resultSet.getString("categoryList"), userListType));
        group.fixList();
        return group;
    }
}
