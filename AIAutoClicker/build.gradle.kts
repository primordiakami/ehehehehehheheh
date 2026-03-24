// Top-level build file
plugins {
        id("com.android.application") version "8.2.0" apply false
            id("com.chaquo.python") version "15.0.1" apply false
}

buildscript {
        repositories {
                    google()
                            mavenCentral()
                                    maven("https://chaquo.com/maven")
        }
            dependencies {
                        classpath("com.android.tools.build:gradle:8.2.0")
                                classpath("com.chaquo.python:gradle:15.0.1")
            }
}
            }
        }
}
}