(ns album.importer
  (:require [clj-time.format :as tf]
            [me.raynes.fs :as fs])
  (:import [com.drew.imaging ImageMetadataReader]))

(defn metadata
  "Get all image metadata for a file"
  [file]
  {:pre [(instance? java.io.File file)]
   :post [(map? %)]}
  (let [metadata (ImageMetadataReader/readMetadata file)]
    (apply merge
           (for [dir (.getDirectories metadata)]
             {(.getName dir)
              (apply merge
                     (for [tag (.getTags dir)]
                       (let [tag-id (.getTagType tag)]
                         {(.getTagName dir tag-id) (.getDescription tag)})))}))))

(defn date-time-original
  [metadata]
  {:pre [(get-in metadata ["Exif SubIFD" "Date/Time Original"])]
   :post [(instance? org.joda.time.DateTime %)]}
  (tf/parse (tf/formatter "yyyy:MM:dd HH:mm:ss")
            (get-in metadata ["Exif SubIFD" "Date/Time Original"])))

(defn date-path
  [basedir date]
  (fs/file basedir
           (str
            (tf/unparse (tf/formatter "yyyy/MM/dd/yyyyMMdd_HHmmss")
                        date)
            ".jpeg")))

(defn import!
  "Move images from an inbox directory to a directory structure
   based on the dates the images were taken"
  [inbox-dir target-dir]
  {:pre [(fs/readable? inbox-dir)]}
  (doseq [sourcefile (fs/find-files inbox-dir #".*\.JPG")]
    (let [targetfile (->> sourcefile
                          metadata
                          date-time-original
                          (date-path target-dir))]
      (fs/mkdirs (fs/parent targetfile))
      (fs/rename sourcefile targetfile))))
