package space.meetable.meetableback

import org.springframework.boot.runApplication

object RegroupAlgorithm {
    /**
     * Generate group plans and pick the one that leaves the least amount of students behind.
     *
     * This algorithm uses random sampling to try and make the groups unpredictable. It's not
     * fun to always get assigned into the same group because a few people have the same niche
     * interests.
     *
     * One area of improvement in the future is to account for the groups that students were in
     * during past weeks, and try to avoid assigning too many people into the same group
     * 3+ weeks in a row.
     *
     * Another future plan is the buddy system, where two students can pair up with each other
     * and agree on their interests. This will guarantee them to be placed into the same group
     * the next week. It will help people discover new interests and help shy people get along
     * with others with the help of an existing friend.
     */
    fun regroup(students: List<Student>): List<Group> {
        var bestPlan: List<Group>? = null
        var bestPlanUngroupedStudents: Int = Int.MAX_VALUE

        for(i in 1..100) {
            val plan = generateGroupPlan(students)

            val ungroupedStudents = students - plan.map{it.students}.flatten()

            if(ungroupedStudents.size < bestPlanUngroupedStudents) {
                bestPlan = plan
                bestPlanUngroupedStudents = ungroupedStudents.size
            }
        }

        return bestPlan!!
    }

    /**
     * Generate group plan
     *
     * Performance considerations: consider a max of a few hundred thousand users per university,
     * with 1000 different interests
     * We want it within O(students^2) or O(interests^3) time complexity (not rly restrictive)
     */
    fun generateGroupPlan(students: List<Student>): List<Group> {
        // Randomly shuffle students
        val sorted = students.shuffled()

        // Generate initial group plan putting every student into their primary interest
        val studentsByInterest = LinkedHashMap<String, ArrayList<Student>>()
        for(student in students) {
            studentsByInterest.getOrPut(student.primaryInterest){ArrayList()} += student
        }

        // For students in primary interests with <4 students, check if their secondary interest has
        // >=3 students. If ALL students in an interest can be reassigned this way, then go ahead
        // and do it
        for(interest in ArrayList(studentsByInterest.keys)) {
            if(studentsByInterest[interest]!!.size < 4) {
                var reassignable = true
                for(s in studentsByInterest[interest]!!) {
                    if(s.primaryInterest == s.secondaryInterest) {
                        reassignable = false
                        break
                    }
                    if(studentsByInterest[s.secondaryInterest]?.let{it.size >= 3} == false) {
                        reassignable = false
                        break
                    }
                }
                if(reassignable) {
                    for(s in studentsByInterest[interest]!!) {
                        studentsByInterest[s.secondaryInterest]?.add(s)
                    }
                    studentsByInterest.remove(interest)
                }
            }
        }

        // For interests that still only have 2-3 students, check if some other students have it as a
        // secondary interest, if that is the case reassign these students to this interest
        for(interest in studentsByInterest.keys) {
            val size = studentsByInterest[interest]!!.size
            if (size in 2..3) {
                var amountNeeded = 4 - size

                // 50% chance to try and generate a plan that gives a 5-6 person group
                // instead of a 4 person one
                if(Math.random() < 0.5) {
                    amountNeeded += 1
                    if(Math.random() < 0.5) amountNeeded += 1
                }

                // Clone the students by interest map
                val studentsByInterestClone = HashMap<String, ArrayList<Student>>()
                for((i, ss) in studentsByInterest)
                    studentsByInterestClone[i] = ArrayList(ss)

                // Try to move some students to the interest if it's their secondary interest
                for((i, ss) in studentsByInterestClone) {
                    if(amountNeeded == 0) break
                    for(s in ss) {
                        if(ss.size <= 4) break // If the interest is too small skip the interest
                        if(s.secondaryInterest == interest) {
                            // Move the student
                            ss -= s
                            amountNeeded -= 1
                            if(amountNeeded == 0) break
                        }
                    }
                }

                // If the movement was successful, apply the plan, otherwise ignore it
                if(amountNeeded == 0) {
                    for(i in studentsByInterestClone.keys) {
                        studentsByInterest[i] = studentsByInterestClone[i]!!
                    }
                }
            }
        }

        // For each interest with 4+ students, split the students into groups
        val groups = ArrayList<Group>()
        for((interest, ss) in studentsByInterest) {
            while(ss.size > 0) {
                when(ss.size) {
                    1, 2, 3 -> { // Can't give these people a group!
                        break
                    }
                    4, 5, 6, 7, 8 -> { // Add everyone into group
                        groups.add(Group(interest, ArrayList(ss)))
                        ss.clear()
                    }
                    9, 10 -> { // First 5 students
                        val removes = listOf(ss.removeLast(), ss.removeLast(), ss.removeLast(),
                                ss.removeLast(), ss.removeLast())
                        groups.add(Group(interest, removes))
                    }
                    11, 12, 17, 18 -> { // First 6 students
                        val removes = listOf(ss.removeLast(), ss.removeLast(), ss.removeLast(),
                                ss.removeLast(), ss.removeLast(), ss.removeLast())
                        groups.add(Group(interest, removes))
                    }
                    13, 14, 19, 20, 21 -> { // First 7 students
                        val removes = listOf(ss.removeLast(), ss.removeLast(), ss.removeLast(),
                                ss.removeLast(), ss.removeLast(), ss.removeLast(),
                                ss.removeLast())
                        groups.add(Group(interest, removes))
                    }
                    15, 16, 22, 23 -> { // First 8 students
                        val removes = listOf(ss.removeLast(), ss.removeLast(), ss.removeLast(),
                                ss.removeLast(), ss.removeLast(), ss.removeLast(),
                                ss.removeLast())
                        groups.add(Group(interest, removes))
                    }
                    else -> { // Groups of 6 until less than 24 students left
                        val removes = listOf(ss.removeLast(), ss.removeLast(), ss.removeLast(),
                                ss.removeLast(), ss.removeLast(), ss.removeLast())
                        groups.add(Group(interest, removes))
                    }
                }
            }
        }

        return groups
    }

}

class Student(val name: String, val primaryInterest: String, val secondaryInterest: String) {
    override fun toString(): String {
        return "$name $primaryInterest $secondaryInterest"
    }
}

class Group(val interest: String, val students: List<Student>)



fun main(args: Array<String>) {
    // Regrouping algorithm test
    val students = listOf(
            Student("Stu", "1", "2"),
            Student("Stu", "1", "2"),
            Student("Stu", "1", "2"),
            Student("Stu", "1", "2"),
            Student("Stu", "1", "2"),
            Student("Stu", "1", "3"),
            Student("Stu", "1", "12"),
            Student("Stu", "1", "3"),
            Student("Stu", "1", "4"),
            Student("Stu", "1", "4"),
            Student("Stu", "1", "5"),
            Student("Stu", "1", "5"),
            Student("Stu", "1", "11"),
            Student("Stu", "1", "6"),
            Student("Stu", "1", "13"),
            Student("Stu", "1", "8"),
            Student("Stu", "1", "10"),
            Student("Stu", "2", "2"),
            Student("Stu", "2", "2"),
            Student("Stu", "3", "2"),
            Student("Stu", "4", "2"),
            Student("Stu", "5", "2"),
            Student("Stu", "2", "3"),
            Student("Stu", "3", "3"),
            Student("Stu", "5", "3"),
            Student("Stu", "7", "11"),
            Student("Stu", "6", "4"),
            Student("Stu", "8", "5"),
            Student("Stu", "9", "5"),
            Student("Stu", "10", "6"),
            Student("Stu", "3", "6"),
            Student("Stu", "5", "14"),
            Student("Stu", "7", "8"),
            Student("Stu", "9", "10"),
            Student("Stu", "2", "2"),
            Student("Stu", "4", "2"),
            Student("Stu", "6", "2"),
            Student("Stu", "8", "2"),
            Student("Stu", "10", "2"),
            Student("Stu", "3", "3"),
            Student("Stu", "4", "3"),
            Student("Stu", "6", "3"),
            Student("Stu", "7", "4"),
            Student("Stu", "8", "4"),
            Student("Stu", "1", "5"),
            Student("Stu", "2", "5"),
            Student("Stu", "4", "6"),
            Student("Stu", "5", "6"),
            Student("Stu", "9", "7"),
            Student("Stu", "10", "8"),
            Student("Stu", "3", "10"),
            Student("Stu", "5", "2"),
            Student("Stu", "6", "2"),
            Student("Stu", "6", "2"),
            Student("Stu", "13", "2"),
            Student("Stu", "14", "15"),
            Student("Stu", "2", "16"),
            Student("Stu", "2", "16"),
            Student("Stu", "1", "16"),
            Student("Stu", "2", "16"),
            Student("Stu", "11", "4"),
            Student("Stu", "3", "5"),
            Student("Stu", "12", "5"),
            Student("Stu", "1", "6"),
            Student("Stu", "2", "6"),
            Student("Stu", "3", "7"),
            Student("Stu", "5", "8"),
            Student("Stu", "5", "10"),
            Student("Stu", "12", "2"),
            Student("Stu", "6", "2"),
            Student("Stu", "1", "2"),
            Student("Stu", "2", "2"),
            Student("Stu", "2", "2"),
            Student("Stu", "12", "3"),
            Student("Stu", "4", "3"),
            Student("Stu", "2", "3"),
            Student("Stu", "12", "4"),
            Student("Stu", "8", "4"),
            Student("Stu", "1", "5"),
            Student("Stu", "2", "5"),
            Student("Stu", "3", "6"),
            Student("Stu", "4", "6"),
            Student("Stu", "5", "7"),
            Student("Stu", "6", "8"),
            Student("Stu", "7", "10"),
    )

    val groups = RegroupAlgorithm.regroup(students)

    for(g in groups) {
        println(g.students)
    }

    System.out.println("Total Students: " + students.size)
    System.out.println("Total Interests: " + (students.map{it.primaryInterest} + students.map{it.secondaryInterest}).toSet().size)
    System.out.println("Total Final Groups: " + groups.size)
    System.out.println("Students put into groups: " + groups.map{it.students.size}.sum())
}
