package net.teamfruit.skcraft.launcher.skins;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;

import com.beust.jcommander.internal.Nullable;
import com.google.common.io.Closer;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.LauncherUtils;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.HttpRequest;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import net.teamfruit.skcraft.launcher.model.skins.SkinInfo;

@Log
@RequiredArgsConstructor
@EqualsAndHashCode(doNotUseGetters = true, of = {"skinInfo", "resourceDir"})
public class SkinData implements Skin {
	private final Launcher launcher;
	private final Skin defaultSkin;
	@Nullable
	private final SkinInfo skinInfo;
	private final File resourceDir;

	@Override
	public String getNewsURL() {
		if (skinInfo==null)
			return defaultSkin.getNewsURL();
		return skinInfo.getNewsURL();
	}

	@Override
	public String getTipsURL() {
		if (skinInfo==null)
			return defaultSkin.getTipsURL();
		return skinInfo.getTipsURL();
	}

	@Override
	public String getSupportURL() {
		if (skinInfo==null)
			return defaultSkin.getSupportURL();
		return skinInfo.getSupportURL();
	}

	private ResourceBundle lang;

	@Override
	public ResourceBundle getLang() {
		if (skinInfo!=null)
			if (lang==null)
		        try {
					lang = new PropertyResourceBundle(new InputStreamReader(new FileInputStream(getLangFile()), "UTF-8"));
				} catch (Exception e) {
					log.log(Level.WARNING, "Could not load skin lang file: ", e);
				}
		if (lang==null)
			return defaultSkin.getLang();
		return lang;
	}

	private File getLangFile() {
		return new File(resourceDir, "lang.properties");
	}

	@Getter(lazy = true, value = AccessLevel.PRIVATE) private final Image backingBackgroundImage = SwingHelper.createImage(skinInfo.getBackgroundURL());

	@Override
	public Image getBackgroundImage() {
		if (skinInfo==null)
			return defaultSkin.getBackgroundImage();
		return getBackingBackgroundImage();
	}

	@Override
	public boolean isShowList() {
		if (skinInfo==null)
			return defaultSkin.isShowList();
		return skinInfo.isShowList();
	}

	@Override
	public String getDefaultModPack() {
		if (skinInfo==null)
			return defaultSkin.getDefaultModPack();
		return skinInfo.getSupportURL();
	}

	@Override
	public void downloadResources() throws Exception {
		if (skinInfo==null) {
			defaultSkin.downloadResources();
			return;
		}

		resourceDir.mkdirs();
		byte[] bytes = HttpRequest
				.get(HttpRequest.url(skinInfo.getLangURL()))
				.execute()
				.expectResponseCode(200)
				.returnContent()
				.saveContent(getLangFile())
				.asBytes();
		Closer closer = Closer.create();
		try {
			lang = new PropertyResourceBundle(closer.register(new InputStreamReader(new ByteArrayInputStream(bytes), "UTF-8")));
		} finally {
			LauncherUtils.closeQuietly(closer);
		}
	}

}