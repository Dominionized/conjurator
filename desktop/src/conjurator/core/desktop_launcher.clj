(ns conjurator.core.desktop-launcher
  (:require [conjurator.core :refer :all])
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [com.badlogic.gdx.backends.lwjgl LwjglApplicationConfiguration]
           [org.lwjgl.input Keyboard])
  (:gen-class))

(defn -main
  []
  (def config (new LwjglApplicationConfiguration))
  (set! (.-resizable config) false)
  (set! (.-title config) "conjurator")
  (set! (.-width config) 800)
  (set! (.-height config) 400)

  (LwjglApplication. conjurator-game config)
  (Keyboard/enableRepeatEvents true))
