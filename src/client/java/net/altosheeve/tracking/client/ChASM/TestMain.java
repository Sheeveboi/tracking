package net.altosheeve.tracking.client.ChASM;

import net.altosheeve.tracking.client.ChASM.Syntax.StandardCompiler;
import net.altosheeve.tracking.client.Soprano.Execution;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class TestMain {
    public static void main(String[] args) throws Exception {
        File extention = new File("test.chasm");
        Scanner extentionReader = new Scanner(extention);
        StringBuilder extentionData = new StringBuilder();
        while (extentionReader.hasNextLine()) extentionData.append(extentionReader.nextLine());
        extentionReader.close();

        System.out.println("Building..");
        ExtendableCompiler e = new StandardCompiler(extentionData.toString().toCharArray());
        System.out.println("Built. Compiling...");

        File program = new File("test." + ExtendableCompiler.programExtension);
        Scanner programReader = new Scanner(program);
        StringBuilder programData = new StringBuilder();
        while (programReader.hasNextLine()) programData.append(programReader.nextLine());
        programReader.close();

        ArrayList<Byte> executable = e.runCompiler(programData.toString());

        System.out.println("compiler result: ");
        System.out.println(executable.toString());

        System.out.println("running kernel: ");

        Execution.setProgram(executable);
        //while(Execution.execute());

    }
}