package cz.droidboy.worktime;

import com.facebook.stetho.Stetho;

/**
 * @author Jonas Sevcik
 */
public class DebugApplication extends App {

    @Override
    public void onCreate() {
        super.onCreate();

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build());
    }
}
