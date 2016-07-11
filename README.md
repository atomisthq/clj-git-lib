# clj-git-lib

[![Build Status](https://travis-ci.com/atomisthq/clj-git-lib.svg?token=iZrpQxJakudjNfb3zxAZ&branch=master)](https://travis-ci.com/atomisthq/clj-git-lib)

Clojure library designed to automate some git operations against a local filesystem (using jgit)

## Usage

The leiningen dependency is:

```
[clj-git-lib "0.1.1"]
```

```
(:require [atomist.git :refer [perform]]
```

There is really just one function called `perform`:

```clj

(defn json-editor 
  "create an editor so update the conatiner image in a k8 spec"
  [new-image-name data] 
  (update-in [:spec :template :containers 0 :image] (constantly new-image-name)))

;; now execute the full set of 
(perform (File. "/Users/slim/repo/atomist-k8-specs")
  {:git-checkout {:branch "prod"}}
  {:edit         {:file-pattern "80-bot-deployment.json" :editor (partial json-editor "new-image")}}
  {:git-add      {:file-pattern "80-bot-deployment.json"}}
  {:git-commit   {:message "here's a new commit message"}}
  {:git-push     {:remote "origin" :branch "prod"}})
```

The first parameter to `perform` should be the location of the git repo that 
 you're working on.  The remaining args are the set of instructions that you
 want to perform within this repo.
 
Today, we just support local jgit instructions, but if we change the first parameter 
to be remote repos, we could support the atomist git service and the tentacles github
api with the same function, and the same "instruction" set.  The key idea would be to to define
data to represent these "instructions" independently of whether it's a local git repo, or a remote one.

The current schemas are [here](https://github.com/atomisthq/clj-git-lib/blob/master/src/atomist/git/schemas.clj).

Hopefully, they're pretty self-explanatory.  I plan to change the perform operation to do a two-pass things where it first validates all of the input and only then runs the instructions, but the first commit doesn't have that.

### Editors

I also want to have very simple editors that take the current file data as input and return the new file data.  For file types, we don't recognize, the data would just be a String, but the file contains `json` or `yaml`, we can pass in editable clojure data structures.  See the `edit` multimethods [at the bottom of core.clj](https://github.com/atomisthq/clj-git-lib/blob/master/src/atomist/git/core.clj#L109)

