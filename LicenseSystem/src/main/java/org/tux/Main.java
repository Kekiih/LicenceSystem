package org.tux;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class Main extends JavaPlugin {
    private String licenseKey;
    private Connection connection;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();

        if (checkMySQLConnection()) {
            if (isLicenseValid()) {
                Bukkit.getLogger().info("Plugin habilitado con licencia: " + licenseKey);
            } else {
                Bukkit.getLogger().severe("Plugin deshabilitado. La licencia no es válida.");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        } else {
            Bukkit.getLogger().severe("Plugin deshabilitado debido a un error en la inicialización de MySQL.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Verificar el número de IPs registradas
        if (!checkMaxIPsRegistered()) {
            Bukkit.getLogger().log(Level.WARNING, "Se han registrado más de 3 IPs para el usuario. Por favor, contacta al técnico del plugin.");
        }
    }

    @Override
    public void onDisable() {
        if (connection != null) {
            try {
                connection.close();
                logInfo("Conexión a MySQL cerrada correctamente.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkMySQLConnection() {
        try {
            String url = "jdbc:mysql://129.213.62.37:3306/s802_license?useSSL=false";
            String user = "u802_adaFVWQeND";
            String password = "o!ZU.^WTnfmJUCzJrvaVjjVQ";
            connection = java.sql.DriverManager.getConnection(url, user, password);

            if (connection.isValid(5)) {
                return true;
            } else {
                logSevere("No se pudo establecer la conexión a la base de datos MySQL.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isLicenseValid() {
        String query = "SELECT * FROM licenses WHERE license_key = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, licenseKey);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean checkMaxIPsRegistered() {
        // Verificar si hay más de 3 IPs registradas para el usuario actual
        String query = "SELECT COUNT(*) AS ip_count FROM user_ips WHERE user_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, getUserIdFromLicenseKey(licenseKey));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int ipCount = resultSet.getInt("ip_count");
                    return ipCount <= 3; // Devolver true si hay 3 o menos IPs registradas
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true; // En caso de error, asumir que está bien
    }

    private String getUserIdFromLicenseKey(String licenseKey) {
        // Aquí deberías implementar la lógica para obtener el ID de usuario a partir de la clave de licencia
        // Esto puede implicar realizar una consulta SQL adicional para obtener el ID de usuario asociado con la clave de licencia
        // Luego, debes retornar el ID de usuario obtenido
        // Por ejemplo:
        // String query = "SELECT user_id FROM licenses WHERE license_key = ?";
        // ...
        return ""; // Aquí deberías retornar el ID de usuario obtenido
    }

    private void logInfo(String message) {
        getLogger().info(colorize("&a" + message));
    }

    private void logSevere(String message) {
        getLogger().severe(colorize("&c" + message));
    }

    private String colorize(String message) {
        return message.replaceAll("&", "§");
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        licenseKey = config.getString("license.key", "LICENSE-HERE");
        getLogger().info("Clave de licencia cargada: " + licenseKey);
    }
}
