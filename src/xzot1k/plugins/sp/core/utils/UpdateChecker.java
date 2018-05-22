package xzot1k.plugins.sp.core.utils;

import xzot1k.plugins.sp.SimplePortals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class UpdateChecker
{

    private SimplePortals pluginInstance;
    private int projectId;
    private URL checkURL;
    private String newVersion;

    public UpdateChecker(SimplePortals pluginInstance, int projectId)
    {
        this.pluginInstance = pluginInstance;
        this.newVersion = pluginInstance.getDescription().getVersion();
        this.projectId = projectId;
        try
        {
            this.checkURL = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + projectId);
        } catch (MalformedURLException ignored) {}
    }

    public int getProjectId()
    {
        return projectId;
    }

    public String getLatestVersion()
    {
        return newVersion;
    }

    public String getResourceURL()
    {
        return "https://www.spigotmc.org/resources/" + projectId;
    }

    public boolean checkForUpdates() throws Exception
    {
        URLConnection con = checkURL.openConnection();
        this.newVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
        return !pluginInstance.getDescription().getVersion().equals(newVersion);
    }

}
