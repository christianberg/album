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

(deftest unit-tests
  (testing "extract Date/Time Original from EXIF data map"
    (let [md {"Exif SubIFD" {"Date/Time Original" "2004:07:03 03:17:10"}}]
      (is (= (date-time-original md)
             (t/date-time 2004 7 3 3 17 10)))))
  (testing "build a path from a date"
    (is (= (date-path (fs/file "foo") (t/date-time 2014 5 2 15 27))
           (fs/file "foo" "2014" "05" "02" "20140502_152700.jpeg")))))

(deftest acceptance
  (testing "metadata is extracted from image file"
    (let [md (metadata (fs/file "resources" "test-images" "IMG123.JPG"))]
      (is (= (get-in md ["Exif SubIFD" "Date/Time Original"])
             "2004:07:03 03:17:10"))))
  (testing "test image is moved from inbox to date-based location"
    (import! (fs/file @basedir "inbox") (fs/file @basedir "originals"))
    (is
     (not
      (fs/file? (fs/file @basedir "inbox" "IMG123.JPG")))
     "Image file is no longer in inbox directory.")
    (is
     (fs/file?
      (fs/file @basedir "originals" "2004" "07" "03" "20040703_031710.jpeg"))
     "Image file is in date-based folder structure in originals directory.")))

