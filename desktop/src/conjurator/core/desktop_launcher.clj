(ns conjurator.core.desktop-launcher
  (:require [conjurator.core :refer :all])
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [org.lwjgl.input Keyboard])
  (:gen-class))

(defn -main
  []
  (LwjglApplication. conjurator-game "conjurator" 800 600)
  (Keyboard/enableRepeatEvents true))
