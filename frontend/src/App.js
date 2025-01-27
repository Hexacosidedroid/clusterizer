import React from "react";
import { Layout, Menu } from "antd";
import { BrowserRouter as Router, Routes, Route, Link } from "react-router-dom";
import Dashboard from "./pages/Dashboard";
import Images from "./pages/Images";
import Containers from "./pages/Containers";

const { Header, Content, Footer, Sider } = Layout;

const App = () => {
  return (
      <Router>
        <Layout style={{ minHeight: "100vh" }}>
          <Sider>
            <Menu theme="dark" mode="inline">
              <Menu.Item key="1">
                <Link to="/">Dashboard</Link>
              </Menu.Item>
              <Menu.Item key="2">
                <Link to="/images">Images</Link>
              </Menu.Item>
              <Menu.Item key="3">
                <Link to="/containers">Containers</Link>
              </Menu.Item>
            </Menu>
          </Sider>
          <Layout>
            <Header style={{ background: "#fff", padding: 0 }} />
            <Content style={{ margin: "16px" }}>
              <Routes>
                <Route path="/" element={<Dashboard />} />
                <Route path="/images" element={<Images />} />
                <Route path="/containers" element={<Containers />} />
              </Routes>
            </Content>
            <Footer style={{ textAlign: "center" }}>Docker Admin ©2025</Footer>
          </Layout>
        </Layout>
      </Router>
  );
};

export default App;