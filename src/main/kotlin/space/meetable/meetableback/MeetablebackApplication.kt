package space.meetable.meetableback

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@RestController
class MeetablebackApplication {
    @GetMapping("/api/regroup")
    fun hello(@RequestParam(value = "university", defaultValue = "dev_uni") university: String): String {
        return ("<h1>Regroup $university</h1>")

        /*
        Currently the RegroupAlgorithm can be run on a test dataset in RegroupAlgorithm.kt

         Future plans -

         this microservice will get students from database, run the algorithm to generate groups,
         and then put the groups back into database.
         */
    }
}


fun main(args: Array<String>) {
    runApplication<MeetablebackApplication>(*args)
}
