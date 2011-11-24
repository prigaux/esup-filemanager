/**
 * Copyright (C) 2011 Esup Portail http://www.esup-portail.org
 * Copyright (C) 2011 UNR RUNN http://www.unr-runn.fr
 * @Author (C) 2011 Vincent Bonamy <Vincent.Bonamy@univ-rouen.fr>
 * @Contributor (C) 2011 Jean-Pierre Tran <Jean-Pierre.Tran@univ-rouen.fr>
 * @Contributor (C) 2011 Julien Marchal <Julien.Marchal@univ-nancy2.fr>
 * @Contributor (C) 2011 Julien Gribonvald <Julien.Gribonvald@recia.fr>
 * @Contributor (C) 2011 David Clarke <david.clarke@anu.edu.au>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 *
 */
package org.esupportail.portlet.stockage.services.cifs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import jcifs.Config;
import jcifs.smb.NtStatus;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.esupportail.portlet.stockage.beans.DownloadFile;
import org.esupportail.portlet.stockage.beans.JsTreeFile;
import org.esupportail.portlet.stockage.beans.SharedUserPortletParameters;
import org.esupportail.portlet.stockage.beans.UserPassword;
import org.esupportail.portlet.stockage.exceptions.EsupStockException;
import org.esupportail.portlet.stockage.services.FsAccess;
import org.esupportail.portlet.stockage.services.ResourceUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.FileCopyUtils;

public class CifsAccessImpl extends FsAccess implements DisposableBean {

	protected static final Log log = LogFactory.getLog(CifsAccessImpl.class);

	protected ResourceUtils resourceUtils;

	private NtlmPasswordAuthentication userAuthenticator;

	protected SmbFile root;

	protected boolean showHiddenFiles = false;

	/** CIFS properties */
	protected Properties jcifsConfigProperties;

	public void initializeService(Map userInfos, SharedUserPortletParameters userParameters) {
		// we set the jcifs properties given in the bean for the drive
		if (this.jcifsConfigProperties != null && !this.jcifsConfigProperties.isEmpty()) {
			Config.setProperties(jcifsConfigProperties);
		}
		super.initializeService(userInfos, userParameters);
	}

	public void setJcifsConfigProperties(Properties jcifsConfigProperties) {
		this.jcifsConfigProperties = jcifsConfigProperties;
	}

	public void setShowHiddenFiles(boolean showHiddenFiles) {
		this.showHiddenFiles = showHiddenFiles;
	}

	@Override
	public void open(SharedUserPortletParameters userParameters) {
		try {
			if(userAuthenticatorService != null && !this.isOpened() ) {
				UserPassword userPassword = userAuthenticatorService.getUserPassword(userParameters);
				userAuthenticator = new NtlmPasswordAuthentication(userPassword.getDomain(), userPassword.getUsername(), userPassword.getPassword()) ;
					SmbFile smbFile = new SmbFile(this.getUri(), userAuthenticator);
					if (smbFile.exists()) {
						root = smbFile;
					}
			}
		} catch (MalformedURLException me) {
			log.error(me, me.getCause());
			throw new EsupStockException(me);
		} catch (SmbAuthException e) {
			if (e.getNtStatus() == NtStatus.NT_STATUS_WRONG_PASSWORD) {
				log.error("connect"+" :: bad password ");
				throw new EsupStockException(e);
			} else if (e.getNtStatus() == NtStatus.NT_STATUS_LOGON_FAILURE) {
				log.error("connect"+" :: bad login ");
				throw new EsupStockException(e);
			} else {
				log.error("connect"+" :: "+e);
				throw new EsupStockException(e);
			}
		} catch (SmbException se) {
			log.error("connect"+" :: "+se);
			throw new EsupStockException(se);
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		log.info("Close : Nothing to do with jcifs!");
		this.root = null;
	}

	/**
	 * @return
	 */
	@Override
	public boolean isOpened() {
			return (this.root != null) ;
	}

	private SmbFile cd(String path, SharedUserPortletParameters userParameters) {
		try {
			this.open(userParameters);
			if (!path.endsWith("/"))
				path = path.concat("/");
			if (path == null || path.length() == 0)
				return root;
			return new SmbFile(this.getUri() + path, userAuthenticator);
		} catch (MalformedURLException me) {
			log.error(me.getMessage());
			throw new EsupStockException(me);
		}
	}

	/**
	 * @param path
	 * @return
	 */
	@Override
	public JsTreeFile get(String path, SharedUserPortletParameters userParameters, boolean folderDetails) {
		try {
			this.open(userParameters);
			if (!path.endsWith("/"))
				path = path.concat("/");
			SmbFile resource = cd(path, userParameters);
			return resourceAsJsTreeFile(resource, userParameters, folderDetails);
		} catch (SmbAuthException sae) {
			log.error(sae.getMessage());
			root = null;
			userAuthenticator = null;
			throw new EsupStockException(sae);
		} catch(SmbException se) {
			log.error(se.getMessage());
			throw new EsupStockException(se);
		}
	}

	/**
	 * @param path
	 * @return
	 */
	@Override
	public List<JsTreeFile> getChildren(String path, SharedUserPortletParameters userParameters) {
		List<JsTreeFile> files = new ArrayList<JsTreeFile>();
		try {
			this.open(userParameters);
			if (!path.endsWith("/"))
				path = path.concat("/");
			SmbFile resource = new SmbFile(this.getUri() + path, userAuthenticator);
			if (resource.canRead()) {
				for(SmbFile child: resource.listFiles()) {
					try {
						if(!child.isHidden() || this.showHiddenFiles) {
							files.add(resourceAsJsTreeFile(child, userParameters, false));
						}
					} catch (SmbException se) {
						log.warn("The resource isn't accessible and so will be ignored", se);
					}
				}
			} else {
				log.warn("The resource can't be read " + resource.toString());
			}
			return files;
		} catch (SmbException se) {
			log.error(se.getMessage());
			throw new EsupStockException(se);
		} catch (MalformedURLException me) {
			log.error(me.getMessage());
			throw new EsupStockException(me);
		}
	}

	private JsTreeFile resourceAsJsTreeFile(SmbFile resource, SharedUserPortletParameters userParameters, boolean folderDetails) throws SmbException {
		String lid = resource.getCanonicalPath();
		String rootPath = root.getCanonicalPath();
		// lid must be a relative path from rootPath
		if(lid.startsWith(rootPath))
			lid = lid.substring(rootPath.length());
		if(lid.startsWith("/"))
			lid = lid.substring(1);

		String title = "";
		String type = "drive";
		if(!"".equals(lid)) {
			if (resource.isDirectory()) {
				type = "folder";
			} else if (resource.isFile()) {
				type = "file";
			} else {
				type = "imaginary";
			}
			title = resource.getName().replace("/", "");
		}
		JsTreeFile file = new JsTreeFile(title, lid, type);
		if("file".equals(type)) {
			String icon = getResourceUtils().getIcon(title);
			file.setIcon(icon);
			file.setSize(resource.getContentLength());
		}

		if(folderDetails && ("folder".equals(type) || "drive".equals(type))) {
			if (resource.listFiles() != null) {
				long totalSize = 0;
				long fileCount = 0;
				long folderCount = 0;
				for (SmbFile child : resource.listFiles()) {
					if (this.showHiddenFiles || !child.isHidden()) {
						if (child.isDirectory()) {
							++folderCount;
						} else if (child.isFile()) {
							++fileCount;
							totalSize += child.getContentLength();
						}
					}
				}
				file.setTotalSize(totalSize);
				file.setFileCount(fileCount);
				file.setFolderCount(folderCount);
			}
		}

		file.setLastModifiedTime(new SimpleDateFormat(this.datePattern).format(resource.getLastModified()));
		file.setHidden(resource.isHidden());
		file.setWriteable(resource.canWrite());
		file.setReadable(resource.canRead());
		return file;
	}

	@Override
	public boolean remove(String path, SharedUserPortletParameters userParameters) {
		boolean success = false;
		SmbFile file;
		try {
			file = cd(path, userParameters);
			file.delete();
			success = true;
		} catch (SmbException e) {
			log.info("can't delete file because of SmbException : "
					+ e.getMessage(), e);
			success = false;
		}
		log.debug("remove file " + path + ": " + success);
		return success;
	}

	@Override
	public String createFile(String parentPath, String title, String type, SharedUserPortletParameters userParameters) {
		try {
			String ppath = parentPath;
			if (!ppath.isEmpty() && !ppath.endsWith("/")) {
				ppath = ppath + "/";
			}
			SmbFile newFile = new SmbFile(root.getPath() + ppath + title, this.userAuthenticator);
			log.info("newFile : " + newFile.toString());
			if ("folder".equals(type)) {
				newFile.mkdir();
				log.info("folder " + title + " created");
			} else {
				newFile.createNewFile();
				log.info("file " + title + " created");
			}
			return newFile.getPath();
		} catch (SmbException e) {
			//log.info("file " + title + " already exists !");
			log.info("can't create file because of SmbException : "
					+ e.getMessage(), e);
		} catch (MalformedURLException e) {
			log.error("problem in creation file that must not occur. " +  e.getMessage(), e);
		}
		return null;
	}

	@Override
	public boolean renameFile(String path, String title, SharedUserPortletParameters userParameters) {
		try {
			SmbFile file = cd(path, userParameters);
			SmbFile newFile = new SmbFile(file.getParent() + title, this.userAuthenticator);
			if (file.exists()) {
				file.renameTo(newFile);
				return true;
			}
		} catch (SmbException e) {
			//log.info("file " + title + " already exists !");
			log.info("can't rename file because of SmbException : "
					+ e.getMessage(), e);
		}  catch (MalformedURLException e) {
			log.error("problem in renaming file." +  e.getMessage(), e);
		}
		return false;
	}

	@Override
	public boolean moveCopyFilesIntoDirectory(String dir,
			List<String> filesToCopy, boolean copy, SharedUserPortletParameters userParameters) {
		try {
			SmbFile folder = cd(dir, userParameters);
			for (String fileToCopyPath : filesToCopy) {
				SmbFile fileToCopy = cd(fileToCopyPath, userParameters);
				SmbFile newFile = new SmbFile(folder.getCanonicalPath() + fileToCopy.getName(), this.userAuthenticator);
				if (copy) {
					fileToCopy.copyTo(newFile);
				} else {
					fileToCopy.copyTo(newFile);
					this.remove(fileToCopyPath, userParameters);
				}

			}
			return true;
		} catch (SmbException e) {
			log.warn("can't move/copy file because of SmbException : "	+ e.getMessage(), e);
		} catch (MalformedURLException e) {
			log.error("problem in creation file that must not occur." +  e.getMessage(), e);
		}
		return false;
	}

	@Override
	public DownloadFile getFile(String dir, SharedUserPortletParameters userParameters) {
		try {
			SmbFile file = cd(dir, userParameters);
			String contentType = file.getContentType();
			int size = new Long(file.getContentLength()).intValue();
			InputStream inputStream = file.getInputStream();
			DownloadFile dlFile = new DownloadFile(contentType, size, file.getName(), inputStream);
			return dlFile;
		} catch (SmbException e) {
			log.warn("can't download file : " + e.getMessage(), e);
		} catch (IOException e) {
			log.error("problem in downloading file." +  e.getMessage(), e);
		}
		return null;
	}

	/**
	 * @param dir
	 * @param filename
	 * @param inputStream
	 * @return
	 */
	@Override
	public boolean putFile(String dir, String filename, InputStream inputStream, SharedUserPortletParameters userParameters) {

		try {
			SmbFile folder = cd(dir, userParameters);
			SmbFile newFile = new SmbFile(folder.getCanonicalPath() + filename, this.userAuthenticator);
			newFile.createNewFile();

			OutputStream outstr = newFile.getOutputStream();

			FileCopyUtils.copy(inputStream, outstr);

			return true;

		} catch (SmbException e) {
			log.info("can't upload file : " + e.getMessage(), e);
		} catch (IOException e) {
			log.warn("can't upload file : " + e.getMessage(), e);
		}
		return false;
	}

	public void destroy() throws Exception {
		this.close();
	}

	/**
	 * Getter of attribute resourceUtils
	 * @return <code>ResourceUtils</code> the attribute resourceUtils
	 */
	public ResourceUtils getResourceUtils() {
		return resourceUtils;
	}

	/**
	 * Setter of attribute resourceUtils
	 * @param resourceUtils <code>ResourceUtils</code> the attribute resourceUtils to set
	 */
	public void setResourceUtils(final ResourceUtils resourceUtils) {
		this.resourceUtils = resourceUtils;
	}
}
