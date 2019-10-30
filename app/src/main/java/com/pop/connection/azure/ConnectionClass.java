package com.pop.connection.azure;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionClass {

    String classs = "net.sourceforge.jtds.jdbc.Driver";
    String db = "PoPdb";
    String user = "dkitindigo@indigoserver";
    String password = "911rAtHHHjiji";

    @SuppressLint("NewApi")
    public Connection connectionclass(){

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection = null;
        String ConnectionURL = null;
        try{
            Class.forName(classs);
            ConnectionURL = "jdbc:jtds:sqlserver://indigoserver.database.windows.net:1433;DatabaseName="+db+";user="+user+";password="+password+";encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
            connection = DriverManager.getConnection(ConnectionURL);
        }
        catch(SQLException se){
            Log.e("SQLException: ",se.getMessage());
        }
        catch(ClassNotFoundException e){
            Log.e("ClassNotFoundException: ",e.getMessage());
        }
        catch(Exception e){
            Log.e("Exception: ",e.getMessage());
        }
        return connection;
    }
}
