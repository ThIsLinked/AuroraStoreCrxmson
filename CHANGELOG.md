> **v4.6.0-nightly-25.08.24 (61)**
> * Pulled up to nightly 25.08.24 (include stable 4.6.0);
> * UI correction;
> * Updated localizations;
> * Returned a link to the off. repo of the original author;
> * Updated structure in files (on project lvl);
> * Some conversions in the code and resource files (on project lvl);
> * Minor changed;

> **v4.5.1-nightly-16.08.24 (60)**
> * Pulled up to nightly 16.08.24;
> * UI and UX changes;
> * In the «Updates» tab, all SwipeRefresh shades now inherit the colors of the current theme;
> * Changed the visual appearance and order of the installers list;
> * [Clone by me] The icon now has a Royal Blue color fill to better differentiate between the original and the clone;

> **v4.5.1-nightly-12.08.24 (60)**
> * Pulled up to nightly 12.08.24 (include stable 4.5.1);
> * Returned the ability to download apk's in user-specified external storage rather than to own directory Aurora (to section "/data/data/...");
> * More massive interface layout editing compared to past versions of the mod;
> * A few edits and corrections in the settings;
> * Project refinement and cleanup;
> * More many other other changes, not very significant, that I'll get tired of listing...

> **v4.3.5-nightly-04.11.23 (53)**
> * Pulled up to nightly 04.11.23;
> * Moved the lists that were in the first setup wizard to resources. This allowed them to be translated. The first setup wizard is now fully localized;
> * Added a condition for the list of topics. On Android 10 and higher, the inherit system theme value (light and dark) will be available. On Android 9 and below, the item will be hidden, and the default item will be «Light»;
> * Added a condition for the list of accents. On Android 12 and higher, the inherit system accent value (dynamic color) will be available. On Android 11 and below, the item will be hidden, and the default item will be «Crimson»;
> * Added «Royal Blue» to the list of accents;
> * Updated localizations, corrected some mistakes after ourselves;
> * Hid the conditional tooltip in the application links section of the first setup wizard until better times;
> * Minor rebuilding of the class tree;
> * Minor changes that are not included in a specific item;

> **v4.3.5-nightly-29.10.23 (53)**
> * Pulled up to nightly 29.10.23 (include stable 4.3.5);
> * Complete re-creation of the project. This has improved cleaning, use and convenience;
> * Redesign of the settings wizard;
> * In the settings wizard, added a section «Apps links» for landscape orientation and adapted it;
> * In the initial setup wizard, the first tab had an inactive «Back» button. This means that there is a condition under which she will be available. But physically there is no previous tab. It wasn't logical. Now on the first tab the button will be invisible;
> * Reworked interface in the first setup wizard;
> * In general, special attention was paid to the interface and decals in it for different locations. I didn't look at all the places, but only what caught my eye. I will improve it in the future;
> * Fully custom layout in settings;
> * Updated the titles and summary in the settings, and also added some tooltips there;
> * Expanded the range of text selection on the application page. I was focusing on what the user might need to highlight and copy. Configured by global value is «details_textIsSelectable» to `values\bools.xml`;
> * Added attr `android:autoVerify` for all deep links. This may help in directing links to Aurora on Android 12 and above. Configured by global value is «appsLinks_verified» in `values\bools.xml`.
> * Changed the frequency range for background checks for application updates from 1-24 hours to 1-48;
> * Fixed bugs known to me;
> * Update all localizations;
> * Optimization and reduction of code volume due to more competent use of components;
> * Even more cleaning of garbage from resources and code;
> * Reworked the self-update layout and adjusted the receive Internet data to Crxmson;
> * Based on the fact that some users do not want to check for self-updates, I added a switch in the settings. Its turning it on and off respectively turns on and off the verification function;
> * Technical: Anonymous account on the author's clone does not work, stable error 502. He will not be in this update.

> **v4.3.2-nightly-25.09.23 (50)**
> * Pulled up to nightly 25.09.23;
> * Aligned the "Update all" button in the updates section;
> * Now the search box background inherits the color colorAccent with an opacity of 10%;
> * Now the app changelog background inherits the color colorAccent with an opacity of 10%;
> * Moved the Aurora folder to "/sdcard/Download/". Somehow I forgot to do it in the beginning. Renamed the folder itself to "AuroraStore". Also renamed "SpoofConfigs" to "Spoof";
> * I thought that a separate folder with the version code was really unnecessary. Now when downloading, the path will look like this: ".../AuroraStore/Downloads/com.android.app.01010/base.apk" instead of ".../com.android.app/01010/base.apk";
> * Removed the colorAccent color inheritance in the "Crxmson" caption on the About page. It now has a static crimson color;

> **v4.3.2-nightly-22.09.23 (50)**
> * Pulled up to nightly 22.09.23 (include stable 4.3.2);
> * Fixed a couple of bugs;
> * Removed forced tinting colorOnPrimary in switch. Now, like last time, tinting is used from inherit the current accent;
> * In the initial setup wizard, the Apps Links section added a scroll for the list by reworking the code;
> * Updated localizations;

> **v4.3.1-nightly-14.09.23 (49)**
> * Pulled up to nightly 14.09.23;
> * Fixed colorPrimaryContainer for switches in "pressed" state;
> * Fixed the background fill for the sections button in the recommendations;
> * Various fixes;

> **v4.3.1-nightly-03.09.23 (49)**
> * Release;
