package de.glowman554.backup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ArgumentParser {
    private final List<String> allowedArgs;
    private final List<String> args;
    private final List<Node> nodes;

    public ArgumentParser(String[] args, String[] allowedArgs) {
        this.args = Arrays.asList(args);
        this.allowedArgs = new ArrayList<>(Arrays.asList(allowedArgs));

        if (!this.allowedArgs.contains("--help")) {
            this.allowedArgs.add("--help");
        }

        this.nodes = new ArrayList<>();
        parse();
    }

    private void parse() {
        for (String arg : this.args) {
            if (arg.startsWith("-")) {
                if (arg.contains("=")) {
                    String[] split = arg.split("=");
                    if (this.allowedArgs.contains(split[0])) {
                        this.nodes.add(new Node(split[0], split[1]));
                    } else {
                        throw new IllegalArgumentException("Invalid argument: " + arg);
                    }
                } else {
                    if (this.allowedArgs.contains(arg)) {
                        this.nodes.add(new Node(arg));
                    } else {
                        throw new IllegalArgumentException("Invalid argument: " + arg);
                    }
                }
            }
        }

        if (isOption("--help")) {
            StringBuilder str = new StringBuilder("Possible arguments:\n");

            for (String allowedArg : this.allowedArgs) {
                str.append("> ").append(allowedArg).append("\n");
            }

            System.out.println(str);
            System.exit(0);
        }
    }

    public String consumeOption(String name, String defaultValue) {
        for (Node node : this.nodes) {
            if (node.name.equals(name)) {
                this.nodes.remove(node);

                if (node.value == null) {
                    throw new IllegalArgumentException("Missing value for argument: " + node.name);
                }

                return node.value;
            }
        }

        if (defaultValue != null) {
            return defaultValue;
        } else {
            throw new IllegalArgumentException("Missing argument: " + name);
        }
    }

    public boolean isOption(String name) {
        for (Node node : this.nodes) {
            if (node.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static class Node {
        String name;
        String value;

        public Node(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public Node(String name) {
            this(name, null);
        }
    }

}

