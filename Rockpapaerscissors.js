const hand = ["가위", "바위", "보"];
const randomIndex = Math.floor(Math.random() * hand.length);
const randomHand = hand[randomIndex];

const userHand = "가위";

console.log(`컴퓨터의 선택: ${randomHand}`);
console.log(`사용자의 선택: ${userHand}`);

if (randomHand == "가위") {
  if (userHand == "바위") console.log("승");
  if (userHand == "보") console.log("패");
  if (userHand == "가위") console.log("무");
} else if (randomHand == "바위") {
  if (userHand == "보") console.log("승");
  if (userHand == "가위") console.log("패");
  if (userHand == "바위") console.log("무");
} else {
  if (userHand == "가위") console.log("승");
  if (userHand == "바위") console.log("패");
  if (userHand == "보") console.log("무");
}
