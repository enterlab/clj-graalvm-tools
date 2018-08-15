# clj-graalvm-tools

A Clojure library containing tools to help you with migrating your Clojure project to GraalVM.

Currently can be used to create script that copies all dependencies from Maven repo to target folder, as found here: https://www.innoq.com/en/blog/native-clojure-and-graalvm/

## Usage

1. No command line yet!
2. No lein integration!
2. DOESN'T WORK WITH `-SNAPSHOT` DEPENDENCIES!

`lein repl`

Then:

```clojure
(require 'clj-graalvm-tools.core)
(in-ns 'clj-graalvm-tools.core)
```

Then find the source project folder you want GraalVM'ified.

Then you simply generate the file, like this:

```clojure
(generate-deps-copy-script
 "/home/luposlip/meew.ee"
 "cp-deps.sh")
```

This generates a script file called `cp-deps.sh` in the folder mentioned. The dependencies are grabbed from the project in the folder specified.

The file looks like this:

```sh
#!/usr/bin/env bash
for archive in \
org/clojure/clojure/1.9.0/clojure-1.9.0.jar \
org/clojure/core.specs.alpha/0.1.24/core.specs.alpha-0.1.24.jar \
org/clojure/spec.alpha/0.1.143/spec.alpha-0.1.143.jar \
...
xml-apis/xml-apis/1.0.b2/xml-apis-1.0.b2.jar; do
(
 cd target/classes && \
 jar xf ~/.m2/repository/$archive
 )
done
```

## License

Copyright © 2018 Enterlab ApS (luposlip)

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
