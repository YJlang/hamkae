import React, { useState } from "react";
import { useNavigate } from "react-router-dom"; // 추가
import { authAPI } from "../lib/authAPI";

const Register = () => {
  const navigate = useNavigate(); // navigate 훅
  const [step, setStep] = useState(1);
  const [formData, setFormData] = useState({
    name: "",
    username: "",
    password: "",
    confirmPassword: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const nextStep = () => {
    setError("");
    if (step === 1 && !formData.name.trim()) return setError("이름을 입력해주세요.");
    if (step === 2) {
      if (!formData.username.trim()) return setError("아이디를 입력해주세요.");
      if (formData.username.length < 6)
        return setError("아이디는 최소 6자 이상이어야 합니다.");
    }
    if (step === 3) {
      if (!formData.password.trim()) return setError("비밀번호를 입력해주세요.");
      if (formData.password.length < 6)
        return setError("비밀번호는 최소 6자 이상이어야 합니다.");
    }
    if (step === 4) {
      if (formData.password !== formData.confirmPassword)
        return setError("비밀번호가 일치하지 않습니다.");
    }
    setStep(step + 1);
  };

  const prevStep = () => {
    setError(""); // 이전으로 가면 에러 초기화
    setStep(step - 1);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    // 최종 제출 시 유효성 확인
    if (!formData.name.trim()) return setError("이름을 입력해주세요.");
    if (!formData.username.trim()) return setError("아이디를 입력해주세요.");
    if (formData.username.length < 6)
      return setError("아이디는 최소 6자 이상이어야 합니다.");
    if (!formData.password.trim()) return setError("비밀번호를 입력해주세요.");
    if (formData.password.length < 6)
      return setError("비밀번호는 최소 6자 이상이어야 합니다.");
    if (formData.password !== formData.confirmPassword)
      return setError("비밀번호가 일치하지 않습니다.");

    setLoading(true);
    try {
      const response = await authAPI.register(
        formData.name,
        formData.username,
        formData.password
      );
      if (response.success) {
        alert("회원가입이 되었습니다! 로그인해주세요.");
        navigate("/login"); // Introduce 페이지로 이동
      } else {
        setError(response.message || "회원가입에 실패했습니다.");
      }
    } catch (error) {
      setError("회원가입 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex justify-center items-center min-h-screen bg-[#73C03F] relative">
      {step > 1 && (
        <img
          src="../../public/arrow.png"
          alt="화살표"
          onClick={prevStep}
          className="absolute top-6 left-6 w-6 h-6 cursor-pointer"
        />
      )}

      <div className="w-80 flex flex-col justify-center items-center min-h-[400px]">
        <div className="w-full">
          {step === 1 && (
            <>
              <h2 className="text-white text-2xl font-bold mb-4">
                이름을<br />
                입력해주세요
              </h2>
              <input
                type="text"
                name="name"
                value={formData.name}
                onChange={handleChange}
                className="w-full border-b border-white bg-transparent text-white p-2 placeholder-white focus:outline-none"
                placeholder="이름"
              />
            </>
          )}

          {step === 2 && (
            <>
              <h2 className="text-white text-2xl font-bold mb-4">
                아이디를 입력해주세요
              </h2>
              <input
                type="text"
                name="username"
                value={formData.username}
                onChange={handleChange}
                className="w-full border-b border-white bg-transparent text-white p-2 placeholder-white focus:outline-none"
                placeholder="아이디를 입력하세요"
              />
            </>
          )}

          {step === 3 && (
            <>
              <h2 className="text-white text-2xl font-bold mb-4">
                비밀번호를 입력해주세요
              </h2>
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                className="w-full border-b border-white bg-transparent text-white p-2 placeholder-white focus:outline-none mb-2"
                placeholder="비밀번호 (6자 이상)"
              />
            </>
          )}

          {step === 4 && (
            <>
              <h2 className="text-white text-2xl font-bold mb-4">
                비밀번호 확인
              </h2>
              <input
                type="password"
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleChange}
                className="w-full border-b border-white bg-transparent text-white p-2 placeholder-white focus:outline-none"
                placeholder="비밀번호 확인"
              />
            </>
          )}
        </div>

        {error && <p className="text-red-300 text-sm mt-2">{error}</p>}

        <div className="flex justify-end mt-4 w-full">
          {step < 4 && (
            <button
              type="button"
              onClick={nextStep}
              className="px-4 py-2 rounded-lg bg-white text-[#73C03F]"
            >
              다음
            </button>
          )}
          {step === 4 && (
            <button
              type="button"
              onClick={handleSubmit}
              disabled={loading}
              className="px-4 py-2 rounded-lg bg-white text-[#73C03F]"
            >
              {loading ? "가입 중..." : "가입하기"}
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default Register;