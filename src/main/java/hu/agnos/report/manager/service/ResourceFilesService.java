/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hu.agnos.report.manager.service;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ruzsaz
 */
public class ResourceFilesService {


    /**
     * Get the list of files in the given directory
     * @param dir Name of the directory
     * @return Set of the files in the directory
     */
    public static Set<String> getResourceFilesSet(String dir) {
        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            return stream
              .filter(file -> !Files.isDirectory(file))
              .map(Path::getFileName)
              .map(Path::toString)
              .collect(Collectors.toSet());
        } catch (IOException ex) {
            LoggerFactory.getLogger(ResourceFilesService.class).error("Error at reading resource dir", ex);
        }
        return new HashSet<>(0);
    }
    
}
