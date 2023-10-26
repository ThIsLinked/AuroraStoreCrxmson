> **v4.3.5-nightly-26.10.23 (53)**
> * Pulled up to nightly 26.10.23 (include stable 4.3.5);
> * Complete re-creation of the project. This has improved cleaning, use and convenience;
> * Redesign of the settings wizard;
> * In the settings wizard, I added a section «Apps links» for landscape orientation and adapted it;
> * In the initial setup wizard, the first tab had an inactive “Back” button. This means that there is a condition under which she will be available. But physically there is no previous tab. It wasn't logical. Now on the first tab the button will be invisible;
> * Reworking the code in the «Apps links» layout made it possible to implement it more concisely, and also lightened the amount of code overall;
> * Reworked some decals in the interface;
> * Expanded the range of text selection on the application page. I was focusing on what the user might need to highlight and copy. Configured by global value "details_textIsSelectable" to values/bools.xml;
> * Updated title and summary text in settings;
> * Fully custom layout in settings;
> * Fixed bugs known to me;
> * Update all localizations;
> * Optimization and reduction of code volume due to more competent use of components;
> * Even more cleaning of garbage from resources and code;

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
