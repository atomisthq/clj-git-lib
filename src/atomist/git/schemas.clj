(ns atomist.git.schemas
  (:require [schema.core :as s]))

(s/defschema EditInstruction  {:edit       {:file-pattern s/Str :editor s/Any}})
(s/defschema WriteInstruction {:write      {:to s/Str :contents s/Str}})
(s/defschema CopyInstruction  {:copy       {:from s/Str :to s/Str}})
(s/defschema MkdirInstruction {:mkdir      {:for s/Str}})
(s/defschema GitAdd           {:git-add    {:file-pattern s/Str}})
(s/defschema GitCommit        {:git-commit {:message      s/Str}})
(s/defschema GitTag           {:git-tag    {:message      s/Str}})
(s/defschema GitPush          {:git-push   {:remote s/Str :branch s/Str}})
(s/defschema ErrorInstruction {:error s/Str})
(s/defschema NoOp {:noop s/Any})
(s/defschema Instruction (s/named (s/either WriteInstruction CopyInstruction MkdirInstruction EditInstruction ErrorInstruction GitAdd GitCommit GitTag GitPush NoOp) "Instruction"))
