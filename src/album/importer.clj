(ns album.importer
  (:require [clj-time.format :as tf])
  (:import [com.drew.imaging ImageMetadataReader]))

(defn metadata
  "Get all image metadata for a file"
  [file]
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
  (tf/parse (tf/formatter "yyyy:MM:dd HH:mm:ss")
            (get-in metadata ["Exif SubIFD" "Date/Time Original"])))

