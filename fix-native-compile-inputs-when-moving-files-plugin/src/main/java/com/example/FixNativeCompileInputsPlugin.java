package com.example;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.cpp.CppBinary;

import static org.gradle.api.tasks.PathSensitivity.RELATIVE;

public class FixNativeCompileInputsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getComponents().withType(CppBinary.class, binary -> {
            TaskProvider<Task> compileTask = project.getTasks().named(compileTaskName(binary), Task.class);
            compileTask.configure(task -> {
                task.getInputs().files(cppSourceOf(binary))
                        .ignoreEmptyDirectories()
                        .withPathSensitivity(RELATIVE);
            });
        });
    }

    private FileCollection cppSourceOf(CppBinary binary) {
        FileCollection result = null;
        if (binary instanceof ExtensionAware) {
            result = (FileCollection) ((ExtensionAware) binary).getExtensions().getExtraProperties().getProperties().get("cppSource");
        }

        if (result == null) {
            result = binary.getCppSource();
        }

        return result;
    }

    //region Names
    private static String qualifyingName(CppBinary binary) {
        String result = binary.getName();
        if (result.startsWith("main")) {
            result = result.substring("main".length());
        } else if (result.endsWith("Executable")) {
            result = result.substring(0, result.length() - "Executable".length());
        }
        return uncapitalize(result);
    }

    private static String compileTaskName(CppBinary binary) {
        return "compile" + capitalize(qualifyingName(binary)) + "Cpp";
    }
    //endregion

    //region StringUtils
    private static String uncapitalize(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    private static String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
    //endregion
}
