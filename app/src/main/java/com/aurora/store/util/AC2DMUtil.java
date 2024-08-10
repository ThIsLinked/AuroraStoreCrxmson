/*
 * Aurora Store
 *  Copyright (C) 2021, Rahul Kumar Patel <whyorean@gmail.com>
 *
 *  Aurora Store is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Aurora Store is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Aurora Store.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.aurora.store.util;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum AC2DMUtil {
    ;

    public static Map<String, String> parseResponse(final String response) {
        final Map<String, String> keyValueMap = new HashMap<>();
        final StringTokenizer st = new StringTokenizer(response, "\n\r");
        while (st.hasMoreTokens()) {
            final String[] keyValue = st.nextToken().split("=", 2);
            if (2 <= keyValue.length) {
                keyValueMap.put(keyValue[0], keyValue[1]);
            }
        }
        return keyValueMap;
    }

    public static Map<String, String> parseCookieString(final String cookies) {
        final Map<String, String> cookieList = new HashMap<>();
        final Pattern cookiePattern = Pattern.compile("([^=]+)=([^;]*);?\\s?");
        final Matcher matcher = cookiePattern.matcher(cookies);
        while (matcher.find()) {
            final String cookieKey = matcher.group(1);
            final String cookieValue = matcher.group(2);
            cookieList.put(cookieKey, cookieValue);
        }
        return cookieList;
    }
}
