scheduler_simulator
====================

## How to config ##
- Clone project and open with Eclipse <br>
- Config build path to compile with 2 jar files in ```libs```
- Edit file ```org.cloudbus.cloudsim.simulate.Simulate```, change testcase file path
- Run file ```org.cloudbus.cloudsim.simulate.Simulate```
- The default ratio is the ratio of total virtual machines between them


## Testcase structure ##
- See ```org.cloudbus.cloudsim.simulate.testcase```
- Testcase is a JSON file contains an array of Partners
- Each partner has ```name```, ```datacenters``` and ```cloudlets``` (apps)
- In ```datacenter```, you must define enough ```ram``` + ```mips``` in ```hosts``` to create virtual machines (which are defined below ```hosts```)
