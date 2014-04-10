(ns album.importer-test
  (:require [clojure.test :refer :all]
            [album.importer :refer :all]
            [me.raynes.fs :as fs]
            [clj-time.core :as t]))

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

(deftest metadata-extraction
  (testing "metadata is extracted from image file"
    (let [md (metadata (fs/file "resources" "test-images" "IMG123.JPG"))]
      (is (= (get-in md ["Exif SubIFD" "Date/Time Original"])
             "2004:07:03 03:17:10"))
      (is (= (date-time-original md)
             (t/date-time 2004 7 3 3 17 10))))))

(deftest acceptance
  (testing "test images are placed into inbox directory"
    (is (fs/file? (fs/file @basedir "inbox" "IMG123.JPG")))))
