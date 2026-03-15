package net.altosheeve.tracking.client.Kernel;

import net.altosheeve.tracking.client.ChASM.ExtendableCompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class TerminalKernel extends BasicFunctions {

    public ExtendableCompiler implementation;
    public static ExtendableCompiler terminalImplementation;

    static {

        File extention = new File("sh.chasm");
        Scanner extentionReader;

        try                             { extentionReader = new Scanner(extention);}
        catch (FileNotFoundException e) { throw new RuntimeException(e); }

        StringBuilder extentionData = new StringBuilder();
        while (extentionReader.hasNextLine()) extentionData.append(extentionReader.nextLine());
        extentionReader.close();

        try                 { terminalImplementation = new ExtendableCompiler(extentionData.toString().toCharArray()); }
        catch (Exception e) { throw new RuntimeException(e); }

    }

    public TerminalKernel(ArrayList<Byte> program, ArrayList<Byte> arguments, BasicFunctions parent) {

        super(program, arguments, parent);

        this.registerInstruction((byte) 0x0, this::_LOAD_IMPLEMENTATION);
        this.registerInstruction((byte) 0x1, this::_RUN);

    }

    public void _LOAD_IMPLEMENTATION() throws Exception {

        String target = Typing._PARSE_STRING(this);

        File extention = new File("/implementations/" + target + ".chasm");
        Scanner extentionReader = new Scanner(extention);
        StringBuilder extentionData = new StringBuilder();
        while (extentionReader.hasNextLine()) extentionData.append(extentionReader.nextLine());
        extentionReader.close();

        this.implementation = new ExtendableCompiler(extentionData.toString().toCharArray());

    }

    public void _RUN() throws Exception {

        String target = Typing._PARSE_STRING(this);

        File program = new File("/programs/" + target);
        Scanner programReader = new Scanner(program);
        StringBuilder programData = new StringBuilder();
        while (programReader.hasNextLine()) programData.append(programReader.nextLine());
        programReader.close();

        Execution.setProgram(this.implementation.runCompiler(programData.toString()));

    }

    public void runCommand(String command) throws Exception {

        Execution.setProgram(terminalImplementation.runCompiler(command));

    }

}
