let numbers = [];

while (numbers.length < 6) {
  let n = Math.floor(Math.random() * 45) + 1;
  if (numbers.indexOf(n) === -1) {
    numbers.push(n);
  }
}
numbers.sort();

console.log(numbers);
