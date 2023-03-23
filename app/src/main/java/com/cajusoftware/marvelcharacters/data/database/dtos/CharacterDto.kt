package com.cajusoftware.marvelcharacters.data.database.dtos

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "character")
data class CharacterDto(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val name: String?,
    val description: String? = null,
    val resourceUri: String? = null,
    val urls: List<UrlsDto>? = null,
    val thumbnail: ThumbnailDto? = null,
    val comics: ComicsDto? = null,
    val stories: StoriesDto? = null,
    val events: EventsDto? = null,
    val series: SeriesDto? = null,
    val copyright: String? = null,
    val nextKey: Int? = null
)
