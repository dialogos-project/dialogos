package com.clt.mac;

import com.apple.mrj.MRJAboutHandler;
import com.apple.mrj.MRJApplicationUtils;
import com.apple.mrj.MRJOpenApplicationHandler;
import com.apple.mrj.MRJPrefsHandler;

class MRJExtendedHandler implements SystemEventAdapter {

    @SuppressWarnings("deprecation")
    public void register(final RequiredEventHandler handler) {
        MRJApplicationUtils.registerOpenApplicationHandler(new MRJOpenApplicationHandler() {
            public void handleOpenApplication() {
                handler.handleOpenApplication();
            }
        });

        if (handler.insertPreferencesItem) {
            MRJApplicationUtils.registerPrefsHandler(new MRJPrefsHandler() {
                public void handlePrefs() {
                    handler.handlePreferences();
                }
            });
        }

        MRJApplicationUtils.registerAboutHandler(new MRJAboutHandler() {
            @Override
            public void handleAbout() {
            }
        });
    }

}
