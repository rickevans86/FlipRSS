package com.fliprss.app

/** Original fictional stories. Resource names keep this model usable in JVM tests. */
data class Article(
    val id: String,
    val topic: String,
    val source: String,
    val title: String,
    val summary: String,
    val image: String,
    val minutes: Int,
    val paragraphs: List<String>,
)

object DemoLibrary {
    val topics = listOf("Nature", "Design", "Technology", "Culture")
    val articles = listOf(
        Article("quiet", "Nature", "FIELD NOTES", "The quiet is out there. Go find it.",
            "A slower route through the mountains, and a different way to see the everyday.", "mountains", 4,
            listOf(
                "The path begins where the road gives up. For a while there is only gravel underfoot, the smell of damp earth, and a line of trees that seems to go on forever. Then the valley opens, and the whole morning changes.",
                "We often travel with a list: a summit to reach, a photograph to take, a distance to record. But some places reward the opposite approach. Stop beside a stream. Watch the light move across the water. Let the route be a suggestion.",
                "A quieter walk does not require a remote mountain range. It might begin in the park at the end of your street, before the traffic wakes up. What matters is giving your attention somewhere to settle.",
                "On the way home, the landscape is the same. Your pace is different. That might be the best thing a day outdoors can offer: a little space to bring back with you.",
            )),
        Article("spaces", "Design", "FORM & FUNCTION", "Rooms that give us room to breathe",
            "Why the most thoughtful spaces start with less.", "interior", 3,
            listOf(
                "A good room does not need to announce itself. A patch of sunlight, a chair in the right place, and a clear surface can do more than a room full of carefully chosen objects.",
                "Designing for daily life means noticing small habits. Where do you put a book when you stop reading? Which corner catches the afternoon light? The answers often matter more than the latest material or colour.",
                "Start by moving something, rather than buying something. Open a view through the room. Bring a chair closer to a window. Leave a little space unplanned. A home should make room for the life that happens inside it.",
            )),
        Article("tools", "Technology", "THE SIGNAL", "Smaller tools. Bigger possibilities.",
            "A fresh look at technology that earns its place in our lives.", "technology", 5,
            listOf(
                "The best tool is sometimes the one you forget you are using. It removes a small obstacle, makes a familiar task easier, and then gets out of the way.",
                "Independent software makers are returning to that idea. Instead of building an app that tries to contain your whole day, they are exploring focused tools that do one useful thing well.",
                "A reading app is a good example. The interesting challenge is not how much information it can display, but how comfortably it helps you spend time with a single story. Good defaults, clear navigation, and reliable behaviour do much of the work.",
                "Possibility does not always arrive as a dramatic new device. Sometimes it looks like a calmer screen, a shorter route through a task, or a tool that leaves you with more attention than it asks for.",
            )),
        Article("forest", "Nature", "FIELD NOTES", "A world beneath the canopy",
            "Take a closer look at the life along a forest trail.", "forest", 4,
            listOf(
                "From a distance, a forest can look like a single green surface. Step inside and it becomes a collection of tiny landscapes: moss on a fallen branch, a pool of light, a seedling growing in an improbable gap.",
                "The pleasure of a familiar trail is learning to notice its changes. Rain darkens the bark. A new leaf unfolds. A bird returns to the same branch. There is no need to name everything to pay attention to it.",
                "Next time you walk through the trees, try stopping for a minute. Look close, then far away. The view has more layers than it first appears.",
            )),
        Article("ocean", "Culture", "WEEKEND JOURNAL", "The art of an unplanned afternoon",
            "Along the coast, a little curiosity is all the itinerary you need.", "ocean", 3,
            listOf(
                "We arrived with no reservation and no particular destination. The afternoon stretched ahead like the shoreline: open, bright, and pleasantly undecided.",
                "There was a second-hand bookshop, a bakery about to close, and a bench looking out to sea. None of them would have made an ambitious itinerary. Together, they made a very good day.",
                "An unplanned afternoon is not wasted time. It is a chance to follow your own interest at its own speed. Sometimes the story you remember is the one you did not set out to find.",
            )),
        Article("city", "Design", "FORM & FUNCTION", "Seeing the city at walking speed",
            "The details that turn a collection of streets into a neighbourhood.", "city", 4,
            listOf(
                "A city feels different on foot. A doorway becomes interesting. A small garden appears between buildings. The street stops being a route and starts being a place.",
                "At walking speed, design is measured in everyday comforts: shade on a warm day, somewhere to sit, a crossing that gives you enough time. These are modest things with a large effect on how a neighbourhood feels.",
                "Try taking a different turn on a familiar journey. Look at the ground floor, not just the skyline. A good city is full of small invitations to stay a little longer.",
            )),
    )

    fun select(topic: String?, savedOnly: Boolean, savedIds: Set<String>): List<Article> =
        articles.filter { (topic == null || it.topic == topic) && (!savedOnly || it.id in savedIds) }
}
