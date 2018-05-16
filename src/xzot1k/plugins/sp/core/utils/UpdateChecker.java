package xzot1k.plugins.sp.core.utils;

import xzot1k.plugins.sp.SimplePortals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker
{

    private SimplePortals pluginInstance;

    public UpdateChecker(SimplePortals pluginInstance)
    {
        this.pluginInstance = pluginInstance;
    }

    public boolean isOutdated()
    {
        try
        {
            HttpURLConnection c = (HttpURLConnection) new URL("http://www.spigotmc.org/api/general.php").openConnection();
            c.setDoOutput(true);
            c.setRequestMethod("POST");
            c.getOutputStream().write(("key=98BE0FE67F88AB82B4C197FAF1DC3B69206EFDCC4D3B80FC83A00037510B99B4&resource=56772").getBytes("UTF-8"));
            String oldversion = pluginInstance.getDescription().getVersion(),
                    newversion = new BufferedReader(new InputStreamReader(c.getInputStream())).readLine();
            if (!newversion.equalsIgnoreCase(oldversion)) return true;
        } catch (Exception ignored)
        {
        }

        return false;
    }

    public String getLatestVersion()
    {
        try
        {
            HttpURLConnection c = (HttpURLConnection) new URL("http://www.spigotmc.org/api/general.php").openConnection();
            c.setDoOutput(true);
            c.setRequestMethod("POST");
            c.getOutputStream().write(("key=98BE0FE67F88AB82B4C197FAF1DC3B69206EFDCC4D3B80FC83A00037510B99B4&resource=56772").getBytes("UTF-8"));
            return new BufferedReader(new InputStreamReader(c.getInputStream())).readLine();
        } catch (Exception ex)
        {
            return pluginInstance.getDescription().getVersion();
        }
    }
}
