package io.github.algomaster99.terminator.index;

import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(
        name = "index",
        mixinStandardHelpOptions = true,
        subcommands = {JdkIndexer.class},
        description = "Create an index of the classfiles")
public class Index implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        new CommandLine(this).usage(System.out);
        return 0;
    }

    public static void main(String[] args) {
        new CommandLine(new Index()).execute(args);
    }
}
