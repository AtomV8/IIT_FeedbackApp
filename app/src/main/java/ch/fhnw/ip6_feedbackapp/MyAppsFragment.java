package ch.fhnw.ip6_feedbackapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ch.fhnw.ip6_feedbackapp.AppDetectionService.SOURCE_FEEDBACK_APP;
import static ch.fhnw.ip6_feedbackapp.AppDetectionService.TYPE_DEFAULT;
import static ch.fhnw.ip6_feedbackapp.AppDetectionService.SOURCE_NOTIFICATION;

public class MyAppsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_apps, container, false);
        ListView listView = view.findViewById(R.id.appsList);
        InstalledApps installedApps = new InstalledApps();
        installedApps.start();
        List<AppDetails> appsList =
        Log.d("LISTCOUNT", Integer.toString(appsList.size()));
        AppsListAdapter appsListAdapter = new AppsListAdapter(getActivity(), appsList);
        listView.setAdapter(appsListAdapter);
        Log.d("ADAPTERCOUNT", Integer.toString(appsListAdapter.getCount()));
        return view;
    }

    // Helper method to exclude system apps
    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    // List adapter for list of installed apps
    class AppsListAdapter extends ArrayAdapter<String>{

        Context context;
        List<AppDetails> installedApps;

        AppsListAdapter(Context c, List<AppDetails> apps){
            super(c, R.layout.apps_list_entry);
            this.installedApps = apps;
            this.context = c;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Get the row elements for the view
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View row = layoutInflater.inflate(R.layout.apps_list_entry, parent, false);
            ImageView appImage = row.findViewById(R.id.appIcon);
            TextView appTitle = row.findViewById(R.id.appPageTitle);

            // Fill in the rows
            appImage.setImageDrawable(installedApps.get(position).getAppIcon());
            appTitle.setText(installedApps.get(position).getAppName());
            final String packageName = installedApps.get(position).getPackageName();

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Open current app's page in feedback app if notification body clicked
                    Intent appFeedbackIntent = new Intent(getActivity(), FeedbackActivity.class);
                    appFeedbackIntent.putExtra("App", packageName);
                    appFeedbackIntent.putExtra("Type", TYPE_DEFAULT);
                    appFeedbackIntent.putExtra("Source", SOURCE_FEEDBACK_APP);
                    startActivity(appFeedbackIntent);
                }
            });
            return row;
        }

        @Override
        public int getCount() {
            return installedApps.size();
        }

    }

    private class InstalledApps extends Thread{

        ArrayList<AppDetails> apps;

        private List<AppDetails> getInstalledApps() {
            List<AppDetails> res = new ArrayList<>();
            List<PackageInfo> packs = getContext().getPackageManager().getInstalledPackages(0);
            for (int i = 0; i < packs.size(); i++) {
                PackageInfo p = packs.get(i);
                if ((isSystemPackage(p) == false)) {
                    String packageName = p.packageName;
                    String appName = p.applicationInfo.loadLabel(getContext().getPackageManager()).toString();
                    Drawable icon = p.applicationInfo.loadIcon(getContext().getPackageManager());
                    String packageVersion = p.versionName;
                    res.add(new AppDetails( packageName, appName, icon, packageVersion));
                }
            }
            Collections.sort(res);
            return res;
        }

        @Override
        public void run() {
            getInstalledApps();
        }

        public ArrayList<AppDetails> getApps() {
            return apps;
        }
    }
}
