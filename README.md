<img id="logo" src="README-logo.svg"  alt="Aurora Store Crxmson logo" />

A moddify/fork the original [Aurora Store source project](https://gitlab.com/AuroraOSS/AuroraStore). Developable for non-commercial purposes for end-personal use.

* [Moddification post on the 4PDA forum](https://4pda.to/forum/index.php?showtopic=887569&view=findpost&p=116441910)
* [Topic Aurora Store on the 4PDA forum](https://4pda.to/forum/index.php?showtopic=887569)

*This project is intended for the CIS territory. There is no localizations for other regions. Except for English USA - it acts as default localization.*

**Current status project:** Waiting for updates <img style="width: 30px;" src="README-kitten.webp" alt=" =^.^= " />

###### @ 2024 [ThIsLinked](https://t.me/thislinked) / [dhwh](https://4pda.to/forum/index.php?showuser=9870529)

# Main changes from original
> **Note:**
```
This changelog may have fewer items than what actually appears in the latest builds.
This happens because I may forget to update it or otherwise edit it.
Always check for changes in the real binary build.
```
```
Android Studio mangles the code of some resources at compiling time.
Use the source code only for research or custom variants. The apk's,
published by me, do not have such a defect, since I corrected them from
such a misunderstanding. Use them in case of pure use – they look
exactly as they were supposed to in the specification.
```
<p></p>

> **List:**
* Modernity of the project;
* Redesigned the UI and UX;
* Replaced the list of accents with a list of [Material Design palette](https://m2.material.io/design/color/the-color-system.html#color-theme-creation);
* On the application page, some elements are now available for highlighting with the cursor (so that the text can be copied to the clipboard);
* Redesigned and/or rearranged sections and items in settings;
* Restoring the switch to download files to the device's internal storage. You can specify your own path if you wish.;
* Restoring full functionality of [Deep Links](https://developer.android.com/training/app-links/deep-linking) (edit [#7082](https://4pda.to/forum/index.php?showtopic=887569&view=findpost&p=124420039));
* Added mono-profiles: xxhdpi, API 27, all languages and actuality architectures - ARMv7, ARM64v8, x86 and x64. *The original source of the original configuration – [#2896](https://4pda.to/forum/index.php?showtopic=887569&view=findpost&p=106493433);*
* Redesigned launcher icon: VectorDrawable rendering only, API 26 and above – adaptive method, API 33 and above – monochrome method (optiomal);
* Redesigned banner for Android TV;
* Integrated network configuration to establish cleartext traffic. This allows traffic-modifying firewalls such as AdGuard to be used seamlessly over of Aurora;
* There are many other changes that I can't categorize in the main changelog. Keep an eye on my change logs during updates to keep up to date with changes throughout the repository...

> ### Only in the apk's published in my performance:
> * Removal of obfuscation and/or minification;
> * Removed stock profiles;
> * Cleaned Smali from garbage, include debug lines;
> * Cleaned resources from garbage;
> * Removed mdpi, ldpi and ldrtl;
> * Recycled dex files;
> * Optimization of apk at archive level;
> * Sign dhwh v1+v2+v3 (on 4PDA);
> * Sign ThIsLinked v1+v2+v3 (on the general web);
> <details><summary>Signature dhwh hash</summary>
>
> _**HEX/DEC:** 0x97d83e3e (-1747435970)_
>
> _**CRC32/DEC:** 0x6a8059f7 (1786796535)_
>
> _**MD5:** 050284900ab95f8de385b8552951cbcc_
>
> _**SHA1:** 6e6b12dbb39099654d1043826e7f9480eee29b55_
>
> _**SHA256:** b21ac037532ea9ae47e98afacb9756fb116f0b11c51860c8115d29512a69eb6c_
> </details>
> <details><summary>Signature ThIsLinked hash</summary>
>
> _**HEX/DEC:** 0x6264f009 (1650782217)_
>
> _**CRC32/DEC:** 0xe2e95680 (-488024448)_
>
> _**MD5:** 21247d96e07877efc1867081d6697a56_
>
> _**SHA1:** 052e470e98d916ad731fca81c38a80b5309eea0e_
>
> _**SHA256:** 2d2e593e349bfff9b371228604579d30028719fe13e97d5ca0610d92ea6c948_
> </details>

# Screenshots

*Please note that these are early screenshots and may not reflect current releases.*

<details><summary>[click spoiler]</summary>
<img style="width:192px;" src="_ScreenshortsDemo/Screenshot_1.webp" alt="Screenshot_1" />
<img style="height:585px;" src="_ScreenshortsDemo/Screenshot_2.webp" alt="Screenshot_2" />
<img style="height:585px;" src="_ScreenshortsDemo/Screenshot_3.webp" alt="Screenshot_3" />
<img style="height:585px;" src="_ScreenshortsDemo/Screenshot_4.webp" alt="Screenshot_4" />
<img style="height:585px;" src="_ScreenshortsDemo/Screenshot_5.webp" alt="Screenshot_5" />
<img style="height:585px;" src="_ScreenshortsDemo/Screenshot_6.webp" alt="Screenshot_6" />
<img style="height:585px;" src="_ScreenshortsDemo/Screenshot_7.webp" alt="Screenshot_7" />
<img style="height:585px;" src="_ScreenshortsDemo/Screenshot_8.webp" alt="Screenshot_8" />
<img style="height:585px;" src="_ScreenshortsDemo/Screenshot_9.webp" alt="Screenshot_9" />
<img style="height:585px;" src="_ScreenshortsDemo/Screenshot_10.webp" alt="Screenshot_10" />
<img style="height:585px;" src="_ScreenshortsDemo/Screenshot_11.webp" alt="Screenshot_11" />
<img style="height:585px;" src="_ScreenshortsDemo/Screenshot_12.webp" alt="Screenshot_12" />
<img style="width:480px;" src="_ScreenshortsDemo/Screenshot_13.webp" alt="Screenshot_13" />
</details>

# Gratitudes

* vladrevers ([GitLab](https://gitlab.com/vladrevers), [4PDA](https://4pda.to/forum/index.php?showuser=5081201)) – Help in Smali and educational directions;
* Maximoff ([official site](https://maximoff.su/), [GitLab](https://gitlab.com/maximoff), [4PDA](https://4pda.to/forum/index.php?showuser=4424665)) – Source code for implementing functions;
* tigr1234566 ([Telegram](https://t.me/tommyhellatigr), [4PDA](https://4pda.to/forum/index.php?showuser=6432902)) – Tester;
* master1274 ([GitLab](https://gitlab.com/anikin.rusl), [4PDA](https://4pda.to/forum/index.php?showuser=5042804)) – Tester;

# License

```
This project has inherited the original license (GPL v3.0) provided by the original developer.
Developed for non-commercial purposes primarily for final personal use. Allowed further editing
from yourself face. Use for plagiarism purposes is prohibited. Content created by the community
that contributed to this project, as well as content freely available on global web used here,
de jure inherits the original license, de facto unlicensed still protected by copyrighted.

If you use someone else's material in your work, do not forget who exactly created this material...
```
