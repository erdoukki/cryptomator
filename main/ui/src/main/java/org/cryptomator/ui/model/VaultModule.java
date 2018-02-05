/*******************************************************************************
 * Copyright (c) 2017 Skymatic UG (haftungsbeschränkt).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the accompanying LICENSE file.
 *******************************************************************************/
package org.cryptomator.ui.model;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

import javax.inject.Scope;

import org.apache.commons.lang3.SystemUtils;
import org.cryptomator.common.settings.Settings;
import org.cryptomator.common.settings.VaultSettings;

import dagger.Module;
import dagger.Provides;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

@Module
public class VaultModule {

	private final VaultSettings vaultSettings;

	public VaultModule(VaultSettings vaultSettings) {
		this.vaultSettings = Objects.requireNonNull(vaultSettings);
	}

	@Provides
	@PerVault
	public VaultSettings provideVaultSettings() {
		return vaultSettings;
	}

	@Scope
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@interface PerVault {

	}

	@Provides
	@PerVault
	public NioAdapter provideNioAdpater(Settings settings, WebDavNioAdapter webDavNioAdapter, FuseNioAdapter fuseNioAdapter) {
		NioAdapterImpl impl = NioAdapterImpl.valueOf(settings.usedNioAdapterImpl().get());
		switch (impl) {
			case WEBDAV:
				return webDavNioAdapter;
			case FUSE:
				return fuseNioAdapter;
			default:
				//this should not happen!
				throw new IllegalStateException("Unsupported NioAdapter: " + settings.usedNioAdapterImpl().get());
		}
	}

	//TODO: ask sebi if this should be here

	private final OS os = OS.getCurrentOS();

	private enum OS {
		WINDOWS,
		LINUX,
		MAC;

		public static OS getCurrentOS() {
			if (SystemUtils.IS_OS_WINDOWS) {
				return WINDOWS;
			} else if (SystemUtils.IS_OS_MAC) {
				return MAC;
			} else {
				return LINUX;
			}
		}

	}

	@Provides
	@VaultModule.PerVault
	FuseEnvironment providesFuseEnvironment(WindowsFuseEnvironment windowsFuseEnvironment, LinuxFuseEnvironment linuxFuseEnvironment, MacFuseEnvironment macFuseEnvironment){
		switch (os){
			case LINUX:
				return linuxFuseEnvironment;
			case WINDOWS:
				return windowsFuseEnvironment;
			case MAC:
				return macFuseEnvironment;
			default:
				//TODO: should be better something else returned?
				return null;
		}
	}
}
