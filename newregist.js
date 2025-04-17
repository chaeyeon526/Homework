function sendit() {
  const useremail = document.getElementById("useremail");
  const userpw = document.getElementById("userpw");
  const userpw_re = document.getElementById("userpw");
  const name = document.getElementById("name");
  const ssn1 = document.getElementById("ssn1");
  const ssn2 = document.getElementById("ssn2");
  const fullNumber = String(ssn1.value) + String(ssn2.value);
  const hp = document.getElementById("hp");

  const expIdText = /^[A-Za-z0-9]{4,20}$/;
  const expPwText =
    /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*])[A-Za-z\d!@#$%^&*]{8,20}$/;
  const expNameText = /^[가-힣]+$/;
  const expHpText = /^\d{3}-\d{3,4}-\d{4}$/;

  if (useremail.value === "") {
    alert("이메일 주소를 입력해주세요.");
    useremail.focus();
    return false;
  }

  if (!expPwText.test(userpw.value)) {
    alert(
      "비밀번호는 8자이상 20자이하의 영문자, 숫자, 특수문자를 한 자이상 꼭 포함해야합니다."
    );
    userpw.focus();
    return false;
  }

  if (userpw.value != userpw_re.value) {
    alert("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
    userpw_re.focus();
    return false;
  }

  if (!expNameText.test(name.value)) {
    name.focus();
    alert("이름은 한글로 입력하세요.");
    return false;
  }
  function ssncheck(fullNumber) {
    const checknum = [2, 3, 4, 5, 6, 7, 8, 9, 2, 3, 4, 5];
    let sum = 0;
    for (let i = 0; i < 12; i++) {
      sum += parseInt(fullNumber[i]) * checknum[i];
    }
    let checknum2 = (11 - (sum % 11)) % 10;

    if (parseInt(fullNumber[12]) == checknum2) {
      return 1;
    } else {
      return 2;
    }
  }
  if (ssncheck(fullNumber) == 2) {
    alert("유효하지 않은 주민등록 번호입니다.");
    ssn1.focus();
    return false;
  }
  if (!expHpText.test(hp.value)) {
    alert("휴대폰번호 형식이 일치하지 않습니다.\n-하이픈을 꼭 입력하세요");
    hp.focus();
    return false;
  }
  return true;
}
