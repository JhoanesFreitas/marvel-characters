package com.cajusoftware.marvelcharacters.data.domain.observers

import com.cajusoftware.marvelcharacters.data.domain.CarouselCharacter

open class CarouselCharacterSubject {
    private val observers = mutableListOf<CarouselCharacterObserver>()

    fun notify(items: List<CarouselCharacter>) {
        observers.forEach {
            it.collect(items)
        }
    }

    fun attach(obs: CarouselCharacterObserver) {
        observers.add(obs)
    }

    fun detach(obs: CarouselCharacterObserver) {
        observers.remove(obs)
    }
}