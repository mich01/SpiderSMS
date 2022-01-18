package com.mich01.spidersms.Backend;

public class BackendFunctions
{
    public static boolean CheckRoot()
    {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        finally {
            if(process !=null)
            {
                try {
                    process.destroy();
                }
                catch (Exception ignored){}
            }
        }
        return true;
    }
}
