(ns album.importer-test
  (:require [clojure.test :refer :all]
            [album.importer :refer :all]
            [me.raynes.fs :as fs]))

(def basedir (atom nil))

(defn setup-test-data
  "Create a temporary directory and put test images into the inbox subdir"
  [test-fn]
  (reset! basedir (fs/temp-dir "album-test"))
  (fs/copy-dir (fs/file "resources" "test-images") (fs/file @basedir "inbox"))
  (test-fn)
  (fs/delete-dir @basedir)
  (reset! basedir nil))

(use-fixtures :once setup-test-data)

(deftest acceptance
  (testing "test images are placed into inbox directory"
    (is (fs/file? (fs/file @basedir "inbox" "IMG123.JPG")))))
