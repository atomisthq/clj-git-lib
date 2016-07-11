# clj-git-lib

Clojure library designed to automate some git operations against the local filesystem (using jgit)

## Usage

There is really jut one function called `perform`:

```clj

(defn json-editor [new-image data] 
  (update-in [:spec :template :containers 0 :image] (constantly new-image)))

(perform (File. "git-repo-root")
  {:edit       {:file-pattern "whatever.json" :editor (partial json-editor "new-image")}}
  {:git-add    {:file-pattern "whatever.json"}}
  {:git-commit {:message "here's a new commit message"}}
  {:git-push   {:remote "origin" :branch "master"}})
```

The first parameter to `perform` should be the location of the git repo that 
 you're working on.  The remaining args are the set of instructions that you
 want to perform in the repo.
 
It could be convenient to define editors which take the current data and return
the new data.  If file name patterns end with known file types, the data passed
in and out of the editors will be clojure data structures from things like
json, or yaml parsers.  Otherwise, the data will just be `String`s.

Today, we just support local jgit instructions, but if we change the first parameter 
to be remote repos, we could support the atomist git service and the tentacles github
api with the same function, and the same "instruction" set.
