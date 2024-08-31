package bymrshocker.shockerquestsystem.database;

import bymrshocker.shockerquestsystem.ShockerQuestSystem;
import bymrshocker.shockerquestsystem.data.QuestData;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DatabaseManager {

    private Connection connection;
    private File databaseFile;
    ShockerQuestSystem plugin;

    public DatabaseManager(ShockerQuestSystem plugin) {
        this.plugin = plugin;
        File pluginFolder = new File(plugin.getDataFolder(), "saved");
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }

        databaseFile = new File(pluginFolder, "questsProgress.db");

        try {
            connect(databaseFile);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        DatabaseInitializer initializer = new DatabaseInitializer(connection);

    }


    public void connect(File database) throws SQLException {
        String url = "jdbc:sqlite:"+ database.getAbsolutePath();
        connection = DriverManager.getConnection(url);
    }

    public Connection getConnection() {
        return connection;
    }

    public File getDatabaseFile() {
        return databaseFile;
    }

    public boolean removeIdFromDB(int id) {
        String sql = "DELETE FROM QuestsProgress WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void loadQuestsForPlayer(String name) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, bukkitTask -> {
            List<String> quests = getActiveQuestsFromPlayer(name);

            if (quests.isEmpty()) return;
            for (String quest : quests) {
                if (!plugin.getActiveQuests().containsKey(quest)) {
                    System.out.println("WARNING. Player " + name + " has invalid questID [" + quest + "] in database!");
                    continue;
                }
                plugin.getActiveQuests().get(quest).getPlayersInProgress().add(name);
            }
        });
    }

    public void unloadQuestsForPlayer(String name) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, bukkitTask -> {
            List<String> quests = getActiveQuestsFromPlayer(name);

            if (quests.isEmpty()) return;
            for (String quest : quests) {
                if (!plugin.getActiveQuests().containsKey(quest)) {
                    continue;
                }
                plugin.getActiveQuests().get(quest).getPlayersInProgress().remove(name);
            }
        });
    }





    public void loadAllQuests() {
        Bukkit.getScheduler().runTask(plugin, bukkitTask -> {
            for (QuestData quest : plugin.getActiveQuests().values()) {
                int id = findIdByColumnValue("questID", quest.getQuestID());
                if (id != -1) {
                    ResultSet rs = getQuestDataFromDB(id);
                    String questId = null;
                    String playersInProgress = null;
                    String playersComplete = null;
                    try {
                        questId = rs.getString(1);
                        playersInProgress = rs.getString(2);
                        playersComplete = rs.getString(3);
                    } catch (SQLException e) {

                    }
                    if (questId == null || playersInProgress == null || playersComplete == null) {
                        System.out.println("ERR. Didn't found values in dbID " + id + " for quest " + questId);
                        return;
                    }
                    ArrayList<String> names = new ArrayList<>();
                    Collections.addAll(names, playersInProgress.split(","));

                    //plugin.getActiveQuests().put(quest, names);


                    System.out.println("Found quest with id = " + id);
                } else {
                    System.out.println("WARNING. Found unregistered saved questData with id: " + quest.getQuestID());
                }
            }
        });
    }



    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    //public void saveAllQuests() {
    //    System.out.println("SavingQuestsProgress");
    //    for (QuestData data : plugin.getActiveQuests().values()) {
    //        int id = findIdByColumnValue("questID", data.getQuestID());
    //        if (id == -1) {
    //            saveNewVehicleDataToDB(data);
    //        } else {
    //            saveVehicleDataToDB(id, data);
    //        }
    //    }
    //}

    public void setAllInDB(String key, Object value) {
        String sql = "INSERT INTO QuestsProgress (" + key + ") VALUES (" + String.valueOf(value) + ")";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int findIdByColumnValue(String columnName, String value) {
        String sql = "SELECT id FROM QuestsProgress WHERE " + columnName + " = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, value);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            } else {
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  -1;
    }


    public void addReputationForPlayer(String playerName, String changeFor, double addValue) {

        int id = findIdByColumnValue("player", playerName);
        String data = getStringFromDB(id, "reputation");
        String[] total = data.split(",");
        if (data.isEmpty()) {
            setInDB(id, "reputation", changeFor + "=" + String.valueOf(addValue));
            return;
        }
        int indexToModify = -1;
        String newVal = null;
        for (int i = 0; i < total.length; i++) {
            String[] keyval = total[i].split("=");
            if (keyval[0].equals(changeFor)) {
                double rep = Double.parseDouble(keyval[1]);
                keyval[1] = String.valueOf(rep + addValue);
                indexToModify = i;
                newVal = keyval[0] + "=" + keyval[1];
                total[i] = newVal;
                break;
            }

        }
        List<String> datas = new ArrayList<>();
        if (total.length != 0) {
            datas.addAll(Arrays.stream(total).toList());
        }


        setInDB(id, "reputation", convertListToString(datas));


    }


    public double getReputationFromPlayer(String playerName, String getFor) {
        int id = findIdByColumnValue("player", playerName);
        String[] total = getStringFromDB(id, "reputation").split(",");
        for (int i = 0; i < total.length; i++) {
            String[] keyval = total[i].split("=");
            if (keyval[0].equals(getFor)) {
                return Double.parseDouble(keyval[1]);
            }
        }
        return -1;
    }


    public List<String> getCompletedQuestsFromPlayer(String playerName) {
        return getDataFromPlayer(playerName, "questsComplete");
    }

    public List<String> getActiveQuestsFromPlayer(String playerName) {
        return getDataFromPlayer(playerName, "questsInProgress");
    }

    public void setActiveQuestsForPlayer(String player, List<String> newQuests) {
        setDataForPlayer(player, "questsInProgress", newQuests);
    }

    public void setCompletedQuestsForPlayer(String player, List<String> newQuests) {
        setDataForPlayer(player, "questsComplete", newQuests);
    }


    public List<String> getDataFromPlayer(String playerName, String column) {
        int id = findIdByColumnValue("player", playerName);
        if (id == -1) {
            return new ArrayList<>();
        }
        String data = getStringFromDB(id, column);
        if (data.isEmpty()) return new ArrayList<>();
        return Arrays.stream(data.split(",")).toList();
    }

    public boolean setDataForPlayer(String playerName, String column, List<String> data) {
        int id = findIdByColumnValue("player", playerName);
        if (id == -1) {
            saveNewPlayerDataToDB(playerName);
            id = findIdByColumnValue("player", playerName);
            System.out.println("Player " + playerName + " not found. Creating new column!");
        }
        setInDB(id, column, convertListToString(data));
        return true;
    }



    private String convertListToString(List<String> list) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i == 0) {
                result = new StringBuilder(list.get(i));
                continue;
            }
            result.append(",").append(list.get(i));
        }
        return result.toString();
    }



    public ResultSet getQuestDataFromDB(int id) {
        String sql = "SELECT * FROM QuestsProgress WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void saveNewPlayerDataToDB(String playerName) {
        String sql = "INSERT INTO QuestsProgress (player, questsInProgress, questsComplete, reputation) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            pstmt.setString(2, "");
            pstmt.setString(3, "");
            pstmt.setString(4, "");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void savePlayerDataToDB(int id, QuestData quest) {
        String sql = "UPDATE QuestsProgress SET player = ?, questsInProgress = ?, questsComplete = ?, reputation = ?, WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {;

            pstmt.setInt(5, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public void setInDB(int id, String key, Object value) {
        String sql = "UPDATE QuestsProgress SET " + key + " = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(2, id);
            if (value instanceof Float) {
                pstmt.setFloat(1, (Float) value);
            } else if (value instanceof Double) {
                pstmt.setDouble(1, (Double) value);
            } else if (value instanceof Integer) {
                pstmt.setInt(1, (Integer) value);
            } else if (value instanceof String) {
                pstmt.setString(1, (String) value);
            } else {
                pstmt.setObject(1, value);
            }

            int rowsAffected = pstmt.executeUpdate();


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getStringFromDB(int id, String key) {
        String sql = "SELECT * FROM QuestsProgress WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString(key);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public double getDoubleFromDB(int id, String key) {
        return Double.parseDouble(getStringFromDB(id, key));
    }

    public int getIntegerFromDB(int id, String key) {
        return Integer.parseInt(getStringFromDB(id, key));
    }

    public float getFloatFromDB(int id, String key) {
        return Float.parseFloat(getStringFromDB(id, key));
    }



}
