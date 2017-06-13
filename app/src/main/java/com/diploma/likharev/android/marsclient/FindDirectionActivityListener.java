package com.diploma.likharev.android.marsclient;

import java.util.List;

/**
 * Created by Lirik on 11.06.2017.
 */

public interface FindDirectionActivityListener {

    void onFindDirectionActivityStart();

    void onFindDirectionActivitySuccess(List<Route> route);
}
