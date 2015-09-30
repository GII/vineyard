/*******************************************************************************
 *
 * Copyright (C) 2015 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 * Copyright (C) 2015 Alejandro Paz <alejandropl@lagostelle.com>
 *
 * This file is part of Vineyard.
 *
 * Vineyard is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Vineyard is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Vineyard. If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

package com.hi3project.vineyard.tools;

import com.hi3project.broccoli.bsdf.impl.deployment.ServiceLocatorsVO;
import com.hi3project.broccoli.conf.ProjProperties;
import com.hi3project.broccoli.bsdl.api.ISemanticLocator;
import com.hi3project.broccoli.bsdf.api.deployment.IUnpacker;
import com.hi3project.broccoli.bsdl.impl.SemanticLocator;
import com.hi3project.broccoli.bsdl.impl.exceptions.ParsingException;
import com.hi3project.broccoli.bsdf.exceptions.ServiceDeployException;
import com.hi3project.broccoli.io.BSDFLogger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * 
 */
public class Unpacker implements IUnpacker
{

    public static final String SERVICE_DESCRIPTOR_PREFIX = "service";
    public static final String SERVICE_IMPLEMENTATION_PREFIX = "serviceImpl";
    public static final String SERVICE_ONTOLOGIES_SUBDIRECTORY = "ontologies";
    public static final String SERVICE_IMPLEMENTATION_SUBDIRECTORY = "implementation";
    public static final String SERVICE_LIBRARIES_SUBDIRECTORY = "lib";
    public static final String BSDM_FILE_EXTENSION = ProjProperties.BSDM_FILE_EXTENSION;
    public static final String BSDL_FILE_EXTENSION = ProjProperties.BSDL_FILE_EXTENSION;
    public static final String OWLS_FILE_EXTENSION = "owls";
    public static final String OWL_FILE_EXTENSION = "owl";
    public static final String JAVA_IMPL_FILES_EXTENSION = "jar";

    @Override
    public ServiceLocatorsVO unpackService(ISemanticLocator packedServiceLocator, ISemanticLocator unpackDirLocator) throws ServiceDeployException
    {
        BSDFLogger.getLogger().info("Unpacks service from: " + packedServiceLocator.toString()
                    + " to: " + unpackDirLocator.toString());
        
        byte[] buffer = new byte[1024];
        ServiceLocatorsVO serviceLocators = new ServiceLocatorsVO();

        String outputDir = unpackDirLocator.getURI().getPath();

        // the directory where service is going to be unpacked
        File folder = new File(outputDir);
        if (!folder.exists())
        {
            if (!folder.mkdirs())
            {
                throw new ServiceDeployException("Cannot create directory to deploy service: " + folder, null);
            }
        } else
        {
            if (!folder.isDirectory())
            {
                throw new ServiceDeployException("Not a valid directory to unpack service: " + unpackDirLocator.toString(), null);
            }
        }

        // open a stream to read zipped files
        ZipInputStream zipInputStream;
        try
        {
            zipInputStream = new ZipInputStream(new FileInputStream(packedServiceLocator.getURI().getPath()));
        } catch (FileNotFoundException ex)
        {
            throw new ServiceDeployException("Cannot find packed service at: " + packedServiceLocator.toString(), ex);
        }
        try
        {
            // process the zip entries            
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null)
            {
                if (!zipEntry.isDirectory())
                {
                    File fileForZipEntry = new File(outputDir + File.separator + zipEntry.getName());
                    try
                    {
                        serviceLocators = addFileToServiceLocators(fileForZipEntry, serviceLocators);
                    } catch (ParsingException ex)
                    {
                        throw new ServiceDeployException("Cannot add to unpacked service: " + fileForZipEntry.getCanonicalPath(), ex);
                    }
                    new File(fileForZipEntry.getParent()).mkdirs(); // create all the non previously existing directories

                    try
                    {
                        FileOutputStream fileOutputStream = new FileOutputStream(fileForZipEntry);
                        int len;
                        while ((len = zipInputStream.read(buffer)) > 0)
                        {
                            fileOutputStream.write(buffer, 0, len);
                        }
                    } catch (IOException ex)
                    {
                        throw new ServiceDeployException("Cannot open stream for entry: " + zipInputStream.toString(), ex);
                    }
                }
                zipEntry = zipInputStream.getNextEntry();
            }

        } catch (IOException ex)
        {
            throw new ServiceDeployException("Cannot unpack service at: " + packedServiceLocator.toString(), ex);
        }
        try
        {
            zipInputStream.closeEntry();
            zipInputStream.close();
        } catch (IOException ex)
        {
            throw new ServiceDeployException("Error closing stream at: " + packedServiceLocator.getURI().getPath(), ex);
        }

        return serviceLocators;
    }

    @Override
    public void packService(ISemanticLocator baseDirLocator, ISemanticLocator packedServiceLocator) throws ServiceDeployException
    {
        BSDFLogger.getLogger().info("Packs service from: " + baseDirLocator
                + " to: " + packedServiceLocator);

        ZipOutputStream zipOutputStream = null;

        try
        {
            // open a stream where zip files are going to be written
            FileOutputStream fileOutputStream;
            try
            {
                fileOutputStream = new FileOutputStream(packedServiceLocator.getURI().getPath());
            } catch (FileNotFoundException ex)
            {
                throw new ServiceDeployException("Cannot create packed service at: " + packedServiceLocator.toString(), ex);
            }

            zipOutputStream = new ZipOutputStream(fileOutputStream);
            // get the base directory for this service files
            File baseDir = new File(baseDirLocator.getURI().getPath());
            if (!baseDir.isDirectory())
            {
                throw new ServiceDeployException("Cannot create packed service at: " + packedServiceLocator.toString(), null);
            }

            // get and add the service descriptor file
            File serviceDescriptorFile = getFilteredFileFromBaseDir(
                    baseDir, SERVICE_DESCRIPTOR_PREFIX, BSDM_FILE_EXTENSION);
            try
            {

                addToZipFile(serviceDescriptorFile, serviceDescriptorFile.getName(), zipOutputStream);

            } catch (FileNotFoundException ex)
            {
                throw new ServiceDeployException("Cannot add service descriptor: " + serviceDescriptorFile.getName()
                        + "; to packed service: " + packedServiceLocator.toString(), ex);
            }

            // get the base subdirectory for the service ontology descriptor files
            File ontologiesDir = new File(baseDirLocator.getURI().getPath() + File.separator + SERVICE_ONTOLOGIES_SUBDIRECTORY);
            if (ontologiesDir.exists() && ontologiesDir.isDirectory())
            {
                for (File f : Arrays.asList(getFilteredFilesFromBaseDir(ontologiesDir, "", "")))
                {
                    try
                    {

                        addToZipFile(f, SERVICE_ONTOLOGIES_SUBDIRECTORY + File.separator + f.getName(), zipOutputStream);

                    } catch (FileNotFoundException ex)
                    {
                        throw new ServiceDeployException("Cannot add service ontology: " + f.getName()
                                + "; to packed service: " + packedServiceLocator.toString(), ex);
                    }
                }
            }

            // get the base subdirectory for the service implementation files
            File implDir = new File(baseDirLocator.getURI().getPath() + File.separator + SERVICE_IMPLEMENTATION_SUBDIRECTORY);
            if (implDir.exists() && implDir.isDirectory())
            {

                // get and add the service implementation file
                File serviceImplementationFile = getFilteredFileFromBaseDir(
                        implDir, SERVICE_IMPLEMENTATION_PREFIX, JAVA_IMPL_FILES_EXTENSION);
                try
                {

                    addToZipFile(serviceImplementationFile,
                            SERVICE_IMPLEMENTATION_SUBDIRECTORY + File.separator + serviceImplementationFile.getName(),
                            zipOutputStream);

                } catch (FileNotFoundException ex)
                {
                    throw new ServiceDeployException("Cannot add service implementation: " + serviceImplementationFile.getName()
                            + "; to packed service: " + packedServiceLocator.toString(), ex);
                }

                // get the subdirectory for the service ontology implementation files
                File ontologiesImplDir = new File(implDir.getCanonicalPath() + File.separator + SERVICE_ONTOLOGIES_SUBDIRECTORY);
                if (ontologiesImplDir.exists() && ontologiesImplDir.isDirectory())
                {
                    for (File f : Arrays.asList(getFilteredFilesFromBaseDir(ontologiesImplDir, "", JAVA_IMPL_FILES_EXTENSION)))
                    {
                        try
                        {

                            addToZipFile(f,
                                    SERVICE_IMPLEMENTATION_SUBDIRECTORY + File.separator
                                    + SERVICE_ONTOLOGIES_SUBDIRECTORY + File.separator + f.getName(),
                                    zipOutputStream);

                        } catch (FileNotFoundException ex)
                        {
                            throw new ServiceDeployException("Cannot add service ontology implementation: " + f.getName()
                                    + "; to packed service: " + packedServiceLocator.toString(), ex);
                        }
                    }
                }

                // get the subdirectory for the service library files
                File librariesDir = new File(implDir.getCanonicalPath() + File.separator + SERVICE_LIBRARIES_SUBDIRECTORY);
                if (librariesDir.exists() && librariesDir.isDirectory())
                {
                    for (File f : Arrays.asList(getFilteredFilesFromBaseDir(librariesDir, "", JAVA_IMPL_FILES_EXTENSION)))
                    {
                        try
                        {

                            addToZipFile(f,
                                    SERVICE_IMPLEMENTATION_SUBDIRECTORY + File.separator
                                    + SERVICE_LIBRARIES_SUBDIRECTORY + File.separator + f.getName(),
                                    zipOutputStream);

                        } catch (FileNotFoundException ex)
                        {
                            throw new ServiceDeployException("Cannot add service library: " + f.getName()
                                    + "; to packed service: " + packedServiceLocator.toString(), ex);
                        }
                    }
                }

            }

            zipOutputStream.finish();
            zipOutputStream.flush();

        } catch (IOException ex)
        {
            throw new ServiceDeployException("Cannot create packed service at: " + packedServiceLocator.toString(), ex);
        } finally
        {
            if (null != zipOutputStream)
            {
                try
                {
                    zipOutputStream.close();
                } catch (IOException ex)
                {
                    throw new ServiceDeployException("Cannot create packed service at: " + packedServiceLocator.toString(), ex);
                }
            }
        }
    }

    public static void deleteDirRecursively(String path) throws IOException
    {
        Path directory = Paths.get(path);
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
            {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
            {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

        });
    }

    private static ServiceLocatorsVO addFileToServiceLocators(File file, ServiceLocatorsVO serviceLocators) throws IOException, ParsingException
    {
        if (file.getCanonicalPath().contains(SERVICE_IMPLEMENTATION_SUBDIRECTORY) && file.getName().endsWith(JAVA_IMPL_FILES_EXTENSION))
        {
            if (file.getCanonicalPath().contains(SERVICE_ONTOLOGIES_SUBDIRECTORY))
            {
                // ontology implementation file:
                serviceLocators.addOntologyImplementationLocator(new SemanticLocator(file.getCanonicalPath()));
            } else if (file.getCanonicalPath().contains(SERVICE_LIBRARIES_SUBDIRECTORY))
            {
                // library file:
                serviceLocators.addLibraryLocator(new SemanticLocator(file.getCanonicalPath()));
            } else if (file.getName().startsWith(SERVICE_IMPLEMENTATION_PREFIX))
            {
                // service implementation file:
                serviceLocators.setServiceImplementationLocator(new SemanticLocator(file.getCanonicalPath()));
            }

            return serviceLocators;
        }

        if (file.getCanonicalPath().contains(SERVICE_ONTOLOGIES_SUBDIRECTORY))
        {
            // ontology descriptor file:
            serviceLocators.addOntologyDescriptorLocator(new SemanticLocator(file.getCanonicalPath()));
            return serviceLocators;
        }

        if (file.getName().startsWith(SERVICE_DESCRIPTOR_PREFIX))
        {
            // service descriptor file:
            serviceLocators.setServiceDescriptorLocator(new SemanticLocator(file.getCanonicalPath()));
            return serviceLocators;
        }

        return serviceLocators;
    }

    private static void addToZipFile(File file, String entryPath, ZipOutputStream zos) throws FileNotFoundException, IOException
    {
        FileInputStream fis = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(entryPath);
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0)
        {
            zos.write(bytes, 0, length);
        }

        zos.flush();

        fis.close();
    }

    private static File getFilteredFileFromBaseDir(File baseDir, final String prefix, final String sufix)
    {
        File[] listFiles = getFilteredFilesFromBaseDir(baseDir, prefix, sufix);
        if (listFiles.length > 0 && listFiles[0].isFile())
        {
            return listFiles[0];
        }
        return null;
    }

    private static File[] getFilteredFilesFromBaseDir(File baseDir, final String prefix, final String sufix)
    {
        return baseDir.listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(File f, String name)
            {
                return (name.toLowerCase().startsWith(prefix.toLowerCase())
                        && name.toLowerCase().endsWith(sufix.toLowerCase()));
            }
        });
    }
}
