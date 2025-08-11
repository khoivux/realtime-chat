import { useState } from "react";
import "./login.css";
import { toast } from "react-toastify";
import upload from "../../lib/upload";
import axios from "axios";

const API_URL = "http://localhost:8081/api/v1/auth"; // Đã đổi sang port 8082

const Login = () => {
  const [avatar, setAvatar] = useState({
    file: null,
    url: "",
  });

  const [loading, setLoading] = useState(false);

  const handleAvatar = (e) => {
    if (e.target.files[0]) {
      setAvatar({
        file: e.target.files[0],
        url: URL.createObjectURL(e.target.files[0]),
      });
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setLoading(true);
    const formData = new FormData(e.target);
    const { username, email, password, firstname, lastname, confirmPassword } = Object.fromEntries(formData);

    // VALIDATE INPUTS
    if (!username || !email || !password || !firstname || !lastname || !confirmPassword)
      return toast.warn("Please enter all inputs!");

    try {
      const res = await axios.post(`${API_URL}/register`, {
        username,
        email,
        password,
        firstname,
        lastname,
        confirmPassword
      });
      toast.success(res.data.message || "Account created! You can login now!");
      e.target.reset();
      setAvatar({ file: null, url: "" });
    } catch (err) {
      toast.error(err?.response?.data?.message || "Registration failed!");
    } finally {
      setLoading(false);
    }
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    const formData = new FormData(e.target);
    const { username, password } = Object.fromEntries(formData);
    try {
      const res = await axios.post(`${API_URL}/login`, {
        username,
        password
      });
      const { accessToken } = res.data.data;
      localStorage.setItem("accessToken", accessToken);
      localStorage.setItem("username", username); // Lưu username
      toast.success(res.data.message || "Login successful!");
      window.location.href = "/";
    } catch (err) {
      toast.error(err?.response?.data?.message || "Login failed!");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login">
      <div className="item">
        <h2>Welcome back,</h2>
        <form onSubmit={handleLogin}>
          <input type="text" placeholder="Username" name="username" />
          <input type="password" placeholder="Password" name="password" />
          <button disabled={loading}>{loading ? "Loading" : "Sign In"}</button>
        </form>
      </div>
      <div className="separator"></div>
      <div className="item">
        <h2>Create an Account</h2>
        <form onSubmit={handleRegister}>
          <input type="text" placeholder="First name" name="firstname" />
          <input type="text" placeholder="Last name" name="lastname" />
          <input type="text" placeholder="Username" name="username" />
          <input type="text" placeholder="Email" name="email" />
          <input type="password" placeholder="Password" name="password" />
          <input type="password" placeholder="Confirm Password" name="confirmPassword" />
          <button disabled={loading}>{loading ? "Loading" : "Sign Up"}</button>
        </form>
      </div>
      {/* Social login buttons */}
      <div style={{ marginTop: 32, display: 'flex', flexDirection: 'column', gap: 12 }}>
        <a
          href="http://localhost:8082/oauth2/authorization/google"
          className="flex items-center justify-center gap-2 px-4 py-2 border rounded-lg bg-white text-gray-700 hover:bg-gray-100"
          style={{ textDecoration: 'none', fontWeight: 500 }}
        >
          <img
            src="https://upload.wikimedia.org/wikipedia/commons/4/4a/Logo_2013_Google.png"
            alt="Google"
            style={{ width: 20, height: 20 }}
          />
          Đăng nhập bằng Google
        </a>
        <a
          href="http://localhost:8082/oauth2/authorization/github"
          className="flex items-center justify-center gap-2 px-4 py-2 border rounded-lg bg-white text-gray-700 hover:bg-gray-100"
          style={{ textDecoration: 'none', fontWeight: 500 }}
        >
          <img
            src="https://cdn-icons-png.flaticon.com/512/25/25231.png"
            alt="GitHub"
            style={{ width: 20, height: 20 }}
          />
          Đăng nhập bằng GitHub
        </a>
      </div>
    </div>
  );
};

export default Login;
