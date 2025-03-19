package dev.igorcferreira.musicstreamsync.model

interface EntryData {
    val id: String
    val title: String
    val artworkUrl: String?
    val body: String?
    val footer: String?
}
