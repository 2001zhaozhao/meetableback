#Meetable Backend

The current version includes the complete algorithm to find groups for students on Meetable's weekly regroup.
Each student has a first-choice interest and optionally a second choice.

The algorithm puts the students into groups of 4- to 8-person groups (prefer 6-person groups). Currently it runs the
algorithm multiple times to produce randomness and picks the run that minimizes the number of students left without a
group. However, the algorithm has room for improvement, where it can be made to account for past groups and deliberately
avoid matching the same people together too many times in a row. This way we won't need multiple tries to generate
randomness, and the algorithm could potentially be sped up significantly.

The future plan for this backend is to make it a full Spring microservice that can be started on the cloud each Sunday
to do the regroups.
It will operate as a file service - it will load a student data dump from MySQL and
create a new MySQL dump that contains the group output.
